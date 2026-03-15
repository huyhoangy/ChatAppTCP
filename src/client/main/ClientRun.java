package client.main;

import client.controller.LoginController;
import client.service.ClientService;
import client.ui.LoginGUI;

public class ClientRun {
    public static void main(String[] args) {
        LoginGUI view = new LoginGUI();
        ClientService service = new ClientService(); 
        LoginController controller = new LoginController(view, service);
        controller.init();
        view.setVisible(true);
    }
}