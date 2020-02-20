package it.univr.library.Controller;

import it.univr.library.*;
import it.univr.library.Model.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.math.BigDecimal;
import java.util.*;

public class ControllerEditBook {

    @FXML
    private HBox headerHBox;

    @FXML
    private Button editBookButton;

    @FXML
    private TextField ISBNTextField;

    @FXML
    private CheckBox changeISBNCheckBox;

    @FXML
    private TextField titleTextField;

    @FXML
    private ComboBox<Author> authorComboBox;
    private ObservableList<Author> authors = FXCollections.observableArrayList();

    @FXML
    private ListView<Author> authorListView;
    private ObservableList<Author> oldAuthors = FXCollections.observableArrayList();

    @FXML
    private TextArea descriptionTextArea;

    @FXML
    private TextField publicationYearTextField;

    @FXML
    private ComboBox<PublishingHouse> publishingHouseComboBox;
    private ObservableList<PublishingHouse> publishingHouses = FXCollections.observableArrayList();

    @FXML
    private ComboBox<Genre> genreComboBox;
    private ObservableList<Genre> genres = FXCollections.observableArrayList();

    @FXML
    private ComboBox<Format> formatComboBox;
    private ObservableList<Format> formats = FXCollections.observableArrayList();

    @FXML
    private ComboBox<Language> languageComboBox;
    private ObservableList<Language> languages = FXCollections.observableArrayList();

    @FXML
    private TextField pagesTextField;

    @FXML
    private TextField priceTextField;

    @FXML
    private TextField librocardPointsTextField;

    @FXML
    private Spinner availableQuantitySpinner;

    @FXML
    private Button selectAuthorButton;

    @FXML
    private ComboBox<Integer> numberAuthorsComboBox;
    private ObservableList<Integer> numberAuthors = FXCollections.observableArrayList();

    @FXML
    private Button deleteAuthorButton;

    @FXML
    private ComboBox<String> bookComboBox;
    private ObservableList<Book> books = FXCollections.observableArrayList();

    @FXML
    private TextField imagePathTextField;

    @FXML
    private Button filterButton;

    private Book originalBook; // we use this to keep the original Book
    private User manager;
    private int numberOfAuthors = 0;
    private ArrayList<Author> authorsToLinkToBook = new ArrayList<>();
    private ArrayList<Author> authorsToDelete = new ArrayList<>();
    private Map<Book, Integer> cart;

    @FXML
    private void initialize()
    {
        //***************** FETCH AND POPULATE BOOK INFORMATION *****************//
        populateBooks();
        bookComboBox.setItems(bookIsbnAndTitle(books));
        bookComboBox.getSelectionModel().selectFirst();

        //***************** FETCH AND POPULATE PUBLISHING HOUSE INFORMATION *****************//
        populatePublishingHouses();
        publishingHouseComboBox.setItems(publishingHouses);

        //***************** FETCH AND POPULATE GENRE INFORMATION *****************//
        populateGenres();
        genreComboBox.setItems(genres);

        //***************** FETCH AND POPULATE FORMAT INFORMATION *****************//
        populateFormats();
        formatComboBox.setItems(formats);

        //***************** FETCH AND POPULATE LANGUAGE INFORMATION *****************//
        populateLanguages();
        languageComboBox.setItems(languages);

        //***************** FETCH AND POPULATE NUMBER OF AUTHORS *****************//
        populateNumbAuthorsComboBox();
        numberAuthorsComboBox.setItems(numberAuthors);

        changeISBNCheckBox.setOnAction(this::handleChangeISBNCheckBox);
        filterButton.setOnAction(this::handleFilterButton);
        deleteAuthorButton.setOnAction(this::handleDeleteAuthorButton);
        selectAuthorButton.setOnAction(this::handleSelectAuthorButton);
        editBookButton.setOnAction(this::handleEditBookButton);

        //***************** SPINNER AVAILABLE QUANTITY *****************//
        availableQuantitySpinner.getEditor().textProperty().addListener((obs, oldValue, newValue) -> {
            try
            {
                Integer.parseInt(availableQuantitySpinner.getEditor().textProperty().get());
            }
            catch (IllegalArgumentException e)
            {
                availableQuantitySpinner.getEditor().textProperty().set("0");
                displayAlert("Available quantity must be numerical!\n");
            }
        });

        //***************** FETCH AND POPULATE THE FIELDS WITH THE FIRST BOOK OF CATALOG *****************//
        populateAllfields(books.get(0));
    }

    public void setManager(User manager)
    {
        this.manager = manager;
    }

    public void setCart(Map<Book, Integer> cart) {
        this.cart = cart;
    }

    public void setHeader()
    {
        ControllerHeader controllerHeader = new ControllerHeader();
        controllerHeader.createHeader(manager, headerHBox,cart);
    }

    private void handleChangeISBNCheckBox(ActionEvent actionEvent)
    {
        if(changeISBNCheckBox.isSelected())
            ISBNTextField.setDisable(false);
        if(!changeISBNCheckBox.isSelected())
            ISBNTextField.setDisable(true);
    }

    private void handleFilterButton(ActionEvent actionEvent)
    {
        ModelBooks DBSinglebook = new ModelDatabaseBooks();
        String[] isbn_Title = bookComboBox.getValue().split(" ");
        Book b = DBSinglebook.getSpecificBook(isbn_Title[0]);
        authors.clear();
        populateAllfields(b);
    }

    /**
     *
     * @param actionEvent
     * When click on delete author button, insert the author selected into an arrayList of authors.
     * Check if is null, if everything's fine display message that author was deleted.
     * If there're no more author to delete, just disable "delete author" button.
     */
    private void handleDeleteAuthorButton(ActionEvent actionEvent)
    {
        if(!authorListView.getItems().isEmpty())
        {
            Author authorToDelete = authorListView.getSelectionModel().getSelectedItem();

            if(authorToDelete != null)
            {
                authorsToDelete.add(authorToDelete);
                authorListView.getItems().remove(authorToDelete);
                displayAlertDeleteAuthor("Author successfully removed!");
                if(authorListView.getItems().isEmpty())
                    deleteAuthorButton.setDisable(true);
            }
            else
            {
                displayAlert("Select an author to delete!");
            }
        }
        else
        {
            displayAlert("No author to delete!");
        }
    }

    /**
     *
     * @param actionEvent
     * When click on Edit Book button, first of all check if there're empty or not valid fields.
     * Then if everything's ok, fetch information and create a new book.
     * Update DB for the single book and unlink the various authors to delete.
     * Update information on DB for the single book and link the new author/s to the book.
     * If we change the ISBN, then the book is not the same, and we want to create a new book,
     * marking the old one as not valid anymore.
     */
    private void handleEditBookButton(ActionEvent actionEvent)
    {
        //fetch all fields and create a new book to update the information of the existing book on db
        if(!isAnyFieldEmptyOrNotValid())
        {
            if(!changeISBNCheckBox.isSelected() || !doesISBNExists(ISBNTextField.getText()))
            {
                ModelBooks DBBook = new ModelDatabaseBooks();
                ModelAuthor DBAuthor = new ModelDatabaseAuthor();

                Book book = fetchBookInformation();

                // If we change the ISBN, then we need to create a new book, and flag the old one as not valid
                if(!originalBook.getISBN().equals(ISBNTextField.getText()))
                {
                    DBBook.addNewBook(book);

                    book.setAuthors(authorListView.getItems());

                    for (Author authorToLink: book.getAuthors())
                        DBAuthor.linkBookToAuthors(authorToLink.getId(), book.getISBN());

                    // Flag the old book as not valid
                    DBBook.flagBookAsNotValid(originalBook.getISBN());
                }
                else // Otherwise, just edit the book
                {
                    // Update db on writers, remove the authors take from authorsToDelete arrayList<>

                    for (Author authorToDelete : authorsToDelete) {
                        DBAuthor.deleteLinkBookToAuthors(authorToDelete.getId(), book.getISBN());
                    }

                    // Make query to update book
                    DBBook.updateBook(book);

                    // Make query to link newAuthors
                    for (Author authorToLink : book.getAuthors())
                        DBAuthor.linkBookToAuthors(authorToLink.getId(), book.getISBN());
                }

                // Change scene
                StageManager addEditBooks = new StageManager();
                addEditBooks.setStageAddEditBooks((Stage) editBookButton.getScene().getWindow(), manager, cart);
            }
            else
            {
                displayAlert("A book with this ISBN already exists!");
            }
        }
    }

    /**
     *
     * @param actionEvent
     * When click on select author, check if it's not null and if there's not already in the arrayList.
     *If everything's fine add to arrayList authorsToLinkToBook.
     * Check also how many author, user can insert.
     */
    private void handleSelectAuthorButton(ActionEvent actionEvent)
    {
        numberAuthorsComboBox.setDisable(true);
        Author author = authorComboBox.getValue();

        if(author == null)
        {
            displayAlert("choose an Author!");
        }
        else
        {
            boolean exists = false;
            boolean alreadyAuthor = false;

            for (Author authorToCheck: authorsToLinkToBook)
            {
                if (author.equals(authorToCheck))
                {
                    exists = true;
                    break;
                }
            }

            for (Author authorToCheck: authorListView.getItems())
            {
                if (author.equals(authorToCheck))
                {
                    alreadyAuthor = true;
                    break;
                }
            }

            if(!alreadyAuthor)
            {
                if(!exists)
                {
                    authorsToLinkToBook.add(author);
                    System.out.println(authorsToLinkToBook.toString());

                    if(numberOfAuthors == 1)
                    {
                        selectAuthorButton.setDisable(true);
                        authorComboBox.setDisable(true);
                    }

                    numberOfAuthors--;
                    displayAlertAddAuthor(numberOfAuthors);
                }
                else
                {
                    displayAlert("Author already selected, choose another one!");
                }
            }
            else
            {
                displayAlert("The author selected is already an author for this book, choose another one!");
            }
        }
    }

    /**
     *
     * @param selectedBook
     * This method take the book passed as parameter and set all the information in the various fields.
     * First of all, clears old values and fills in fields with newer ones.
     */
    private void populateAllfields(Book selectedBook)
    {
        //**** CLEAR OLD VALUES ****//
        authorsToDelete.clear();
        authorsToLinkToBook.clear();
        oldAuthors.clear();
        authorListView.setItems(oldAuthors);
        numberAuthorsComboBox.setValue(0);
        numberAuthorsComboBox.setDisable(false);
        deleteAuthorButton.setDisable(true);
        authorComboBox.setDisable(true);
        selectAuthorButton.setDisable(false);
        ISBNTextField.setDisable(true);
        changeISBNCheckBox.setSelected(false);
        numberOfAuthors = 0;

        availableQuantitySpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 1000, selectedBook.getMaxQuantity()));
        availableQuantitySpinner.setEditable(true);

        //**** SET NEW VALUES ****//
        ISBNTextField.setText(selectedBook.getISBN());
        titleTextField.setText(selectedBook.getTitle());
        authorListView.setItems(arrayListToObservableList(selectedBook.getAuthors()));
        descriptionTextArea.setText(selectedBook.getDescription());
        publicationYearTextField.setText(selectedBook.getPublicationYear().toString());
        publishingHouseComboBox.getSelectionModel().select(selectedBook.getPublishingHouse());
        genreComboBox.getSelectionModel().select(selectedBook.getGenre());
        formatComboBox.getSelectionModel().select(selectedBook.getFormat());
        languageComboBox.getSelectionModel().select(selectedBook.getLanguage());
        pagesTextField.setText(selectedBook.getPages().toString());
        priceTextField.setText(selectedBook.getPrice().toString());
        librocardPointsTextField.setText(selectedBook.getPoints().toString());
        imagePathTextField.setText(selectedBook.getImagePath());

        //**** SET LISTENER ON THE COMBOBOX ****//
        numberAuthorsComboBox.getSelectionModel().selectedItemProperty().addListener((v, oldValue, newValue) -> numberOfAuthors = newValue );
        numberAuthorsComboBox.getSelectionModel().selectedItemProperty().addListener((v) -> authorComboBox.setDisable(false));

        //**** SET AUTHORS IN THE COMBOBOX AND SET LISTENER ****//
        populateAuthors();
        authorComboBox.setItems(authors);
        authorListView.setItems(oldAuthors);
        authorListView.getSelectionModel().selectedItemProperty().addListener((v) -> deleteAuthorButton.setDisable(false));

        //**** KEEP THE ORIGINAL BOOK ****//
        originalBook = new Book(selectedBook);
    }

    private Book fetchBookInformation() {
        Book book = new Book();

        book.setAuthors(authorsToLinkToBook);
        book.setDescription(descriptionTextArea.getText().trim());
        book.setFormat(formatComboBox.getValue());
        book.setGenre(genreComboBox.getValue());
        book.setImagePath(imagePathTextField.getText().trim());
        book.setISBN(ISBNTextField.getText().trim());
        book.setLanguage(languageComboBox.getValue());
        book.setMaxQuantity(Integer.parseInt(availableQuantitySpinner.getEditor().textProperty().get()));
        book.setPages(Integer.parseInt(pagesTextField.getText().trim()));
        book.setPoints(Integer.parseInt(librocardPointsTextField.getText().trim()));
        book.setTitle(titleTextField.getText().trim());
        book.setPrice(new BigDecimal(priceTextField.getText().trim()));
        book.setPublicationYear(Integer.parseInt(publicationYearTextField.getText().trim()));
        book.setPublishingHouse(publishingHouseComboBox.getValue());

        return book;
    }

    /**
     *
     * @param authors
     * @return an observable list of Author.
     *
     * This method just takes a List of authors and transforms it into an observableList.
     */
    private ObservableList<Author> arrayListToObservableList(List<Author> authors)
    {
        ObservableList<Author> a = FXCollections.observableArrayList();
        a.addAll(authors);
        return a;
    }

    /**
     *
     * @param books
     * @return an observableList of string with ISBN and Title of single book.
     *
     * This method takes the various ISBN and Title from the single book and creates a string.
     * This observableList is used to fill the BookComboBox.
     */
    private ObservableList<String> bookIsbnAndTitle(ObservableList<Book> books) {
        ObservableList<String> booksIsbnAndTitle = FXCollections.observableArrayList();
        String finalBook;
        for (Book b: books)
        {
            finalBook = b.getISBN() + " " + b.getTitle();
            booksIsbnAndTitle.add(finalBook);
        }
        return booksIsbnAndTitle;
    }

    private void populateBooks()
    {
        ModelBooks DBbooks = new ModelDatabaseBooks();
        books.addAll(DBbooks.getAllBooks());
    }

    private void populateNumbAuthorsComboBox()
    {
        for(int i=1; i<=10;i++)
            numberAuthors.add(i);
    }

    private void populateAuthors()
    {
        ModelAuthor DBauthors = new ModelDatabaseAuthor();
        authors.addAll((DBauthors.getAuthors()));
        oldAuthors.addAll(DBauthors.getAuthorsForSpecificBook(ISBNTextField.getText()));
    }

    private void populatePublishingHouses()
    {
        ModelPublishingHouse DBpublishingHouse = new ModelDatabasePublishingHouse();
        publishingHouses.addAll(DBpublishingHouse.getPublishingHouses());
    }

    private void populateGenres()
    {
        ModelGenres DBgenres = new ModelDatabaseGenres();
        genres.addAll(DBgenres.getGenres());
    }

    private void populateFormats()
    {
        ModelFormat DBformats = new ModelDatabaseFormat();
        formats.addAll(DBformats.getFormats());
    }

    private void populateLanguages()
    {
        ModelLanguage DBlanguages = new ModelDatabaseLanguage();
        languages.addAll((DBlanguages.getLanguages()));
    }

    private boolean isAnyFieldEmptyOrNotValid()
    {
        StringBuilder error = new StringBuilder();
        Optional<ButtonType> result;

        if(ISBNTextField.getText().trim().equals(""))
            error.append("- ISBN must be filled!\n");

        if(!isISBNvalid(ISBNTextField.getText().trim()) && !isASINvalid(ISBNTextField.getText().trim()))
            error.append("- ISBN has no format 10 or 13 or ASIN!\n");

        if(titleTextField.getText().trim().isEmpty())
            error.append("- Title must be filled!\n");

        if(descriptionTextArea.getText().trim().isEmpty())
            error.append("- Description must be filled!\n");
        if(isNumerical(descriptionTextArea.getText().trim()))
            error.append("- Description must be at least alphabetic\n");

        if(publicationYearTextField.getText().trim().isEmpty())
            error.append("- Publication year must be filled\n");

        if(!isNumerical(publicationYearTextField.getText().trim()))
        {
            error.append("- Publication year must be numerical\n");
        }
        else
        {
            int year = Calendar.getInstance().get(Calendar.YEAR);
            if(Integer.parseInt(publicationYearTextField.getText().trim()) < 1000 || Integer.parseInt(publicationYearTextField.getText().trim()) > year)
                error.append(String.format("- Publication year must be between 1800 and %d\n",year));
        }

        if(pagesTextField.getText().trim().isEmpty())
            error.append("- Number of pages must be filled!\n");
        if(!isNumerical(pagesTextField.getText().trim()))
            error.append("- Number of pages must be numerical!\n");

        if(librocardPointsTextField.getText().trim().isEmpty())
            error.append("- LibroCard Points must be filled!\n");
        if(!isNumerical(librocardPointsTextField.getText().trim()))
            error.append("- Librocard Points must be numerical!\n");

        //this 'if' should never happen, but let's keep it for stability
        if(     !isNumerical(availableQuantitySpinner.getEditor().textProperty().get().trim()) ||
                availableQuantitySpinner.getValue() == null ||
                availableQuantitySpinner.getEditor().textProperty().get().trim().equals(""))
        {
            error.append("- Available Quantity must be numerical!\n");
            availableQuantitySpinner.getEditor().textProperty().set("0");
        }
        else
        {
            if (Integer.parseInt(availableQuantitySpinner.getValue().toString().trim()) > 100) {
                result = displayConfirmation("The quantity available is greater 100, are you sure?\n").showAndWait();

                if (result.get() != ButtonType.OK) {
                    displayAlert("Ok select a new available quantity!");
                    return true;
                }
            }
        }

        if(imagePathTextField.getText().trim().isEmpty())
            error.append("- No imagePath specified!\n");

        //check if I've at least an author for the book
        if(authorListView.getItems().isEmpty() && authorsToLinkToBook.isEmpty())
            error.append("- Book needs at least one author!");

        if(priceTextField.getText().trim().isEmpty())
            error.append("- Price must be filled!\n");

        if(!isNumerical(priceTextField.getText().trim()))
        {
            error.append("- Price must be numerical!\n");
        }
        else
        {
            if(Float.parseFloat(priceTextField.getText().trim()) > 1000)
            {
                result = displayConfirmation("The price that you insert is greater than 1000€, are you sure?\n").showAndWait();
                if(result.get() != ButtonType.OK)
                {
                    displayAlert("Ok select a new price!");
                    return true;
                }
            }
        }

        //displayConfirmation();
        if(!error.toString().isEmpty())
            displayAlert(error.toString());

        return !error.toString().isEmpty();
    }

    private boolean doesISBNExists(String ISBN)
    {
        ModelBooks DBBook = new ModelDatabaseBooks();
        return DBBook.doesISBNAlreadyExists(ISBN);
    }

    private void displayAlertAddAuthor(int numberOfAuthors)
    {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Successfully Added");
        alert.setHeaderText(null);
        if(numberOfAuthors > 0)
            alert.setContentText(String.format("Author successfully added, add another %d authors", numberOfAuthors));
        else
            alert.setContentText("Last author successfully added");

        alert.showAndWait();
    }

    private void displayAlert(String s)
    {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Check your input");
        alert.setHeaderText(null);
        alert.setContentText(s);

        alert.showAndWait();
    }

    private Alert displayConfirmation(String s)
    {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation Dialog");
        alert.setHeaderText("Check Book Information!");
        alert.setContentText(s);

        return alert;
    }

    private void displayAlertDeleteAuthor(String s)
    {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Successfully removed!");
        alert.setHeaderText(null);
        alert.setContentText(s);

        alert.showAndWait();
    }

    private boolean isNumerical(String s) {
        return s.matches("[+-]?([0-9]*[.])?[0-9]+");
    }

    private boolean isISBNvalid(String s){ return s.matches("^(?:ISBN(?:-1[03])?:? )?(?=[0-9X]{10}$|(?=(?:[0-9]+[- ]){3})" +
            "[- 0-9X]{13}$|97[89]-[0-9]{10}$|(?=(?:[0-9]+[- ]){4})[- 0-9]{17}$)" +
            "(?:97[89][- ]?)?[0-9]{1,5}[- ]?[0-9]+[- ]?[0-9]+[- ]?[0-9X]$");}

    private boolean isASINvalid(String s)
    {
        return s.matches("\\b(([0-9]{9}[0-9X])|(B[0-9A-Z]{9}))\\b");
    }
}