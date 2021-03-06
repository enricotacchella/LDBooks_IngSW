package it.univr.library.Controller;

import it.univr.library.Data.Book;
import it.univr.library.Model.ModelDatabaseOrder;
import it.univr.library.Model.ModelOrder;
import it.univr.library.Data.Order;
import it.univr.library.Utils.StageManager;
import it.univr.library.Data.User;
import javafx.fxml.FXML;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.event.ActionEvent;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ControllerOrderUnregisteredUser {

    @FXML
    private HBox headerHBox;

    @FXML
    private Button trackOrderButton;

    @FXML
    private TextField trackingCodeTextField;

    @FXML
    private TextField mailTextField;

    private User user;

    private final Pattern VALID_EMAIL_ADDRESS_REGEX =
            Pattern.compile("^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?(?:\\.[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)*$", Pattern.CASE_INSENSITIVE);
    private Map<Book, Integer> cart;

    @FXML
    private void initialize()
    {
        // handle to press enter for login
        trackingCodeTextField.setOnKeyReleased(event ->
        {
            if (event.getCode() == KeyCode.ENTER)
                trackOrderButton.fire();
        });
        trackOrderButton.setOnAction(this::handleTrackOrderButton);
    }

    public void setUser(User user)
    {
        this.user = user;
    }

    public void setCart(Map<Book, Integer> cart) {
        this.cart = cart;
    }

    public void setHeader()
    {
        ControllerHeader controllerHeader = new ControllerHeader();
        controllerHeader.createHeader(user, headerHBox,cart);
    }

    private void handleTrackOrderButton(ActionEvent actionEvent)
    {
        ControllerAlert alerts = new ControllerAlert();

        ModelOrder DBgetOrderUserNotReg = new ModelDatabaseOrder();
        StringBuilder error = new StringBuilder();
        String mailNotRegUser;
        String orderCode;
        ArrayList<Order> order = new ArrayList<>();

        //first, check if every field is compiled
        if(!isAnyFieldNullOrEmpty())
        {
            //if everything's fine, take all text from textfields
            mailNotRegUser = mailTextField.getText();
            orderCode = trackingCodeTextField.getText();

            //if the mail is valid
            if(isMailValid(mailNotRegUser, error))
            {
                order = DBgetOrderUserNotReg.getOrderNotRegisteresUser(mailNotRegUser, orderCode);

                if(order == null || order.isEmpty())
                {
                    alerts.displayAlert("There is not Track Code associates to this mail!\n" + "Check your inputs");
                }
                else //take the order from db
                {
                    //and show it
                    StageManager orderUnregisteredUserStage = new StageManager();
                    orderUnregisteredUserStage.setStageUnregOrderUserView((Stage) trackOrderButton.getScene().getWindow(), user, order, cart);
                }
            }
            else
                alerts.displayAlert(error.toString());
        }
        else
            alerts.displayAlert("All text field must be filled!");
    }

    private boolean isAnyFieldNullOrEmpty()
    {
        return mailTextField == null || mailTextField.getText().isEmpty()
                || trackOrderButton == null || trackingCodeTextField.getText().isEmpty();
    }

    private boolean isMailValid(String emailStr, StringBuilder error) {
        Matcher matcher = VALID_EMAIL_ADDRESS_REGEX .matcher(emailStr);
        if(!matcher.find())
            error.append("- Mail is not in the right format (abc@mail.com)\n");

        return  error.toString().isEmpty();
    }
}
