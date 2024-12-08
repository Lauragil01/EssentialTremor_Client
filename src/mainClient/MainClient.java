package mainClient;

import pojos.Patient;

import java.io.*;
import java.net.Socket;
import java.util.InputMismatchException;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MainClient {
    private static Scanner sc = new Scanner(System.in);
    private static Socket socket;
    private static PrintWriter printWriter;
    private static BufferedReader bufferedReader;
    private static InputStream inputStream;
    private static Patient patient;
    private static boolean control;
    private static int option;

    public static void main(String[] args) {
        try {
            socket = new Socket("localhost", 12345);
            printWriter = new PrintWriter(socket.getOutputStream(), true);
            bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            inputStream = socket.getInputStream();
            boolean connection = true;

            /*if (!login()) {
                System.out.println("Login failed. Exiting.");
                return;
            }*/

            //registerPatient();

            try {
                control = true;
                while (control) {
                    System.out.println("\n@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
                    System.out.println("@@                                                                  @@");
                    System.out.println("@@                 Welcome.                                         @@");
                    System.out.println("@@                 1. Register as user                              @@");
                    System.out.println("@@                 2. Log in                                        @@");
                    System.out.println("@@                 0. Exit                                          @@");
                    System.out.println("@@                                                                  @@");
                    System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
                    System.out.print("\nSelect an option: ");

                    try {
                        option = sc.nextInt();
                        sc.nextLine(); //clean buffer after reading
                    } catch (InputMismatchException e) {
                        System.out.println("Invalid input. Please enter a number.");
                        sc.next();
                        continue;
                    }
                    switch (option) {
                        case 1:
                            registerPatient();
                            break;
                        case 2:
                            loginPatient();
                            break;
                        case 0:
                            System.out.println("Exiting...");
                            //connection = false;
                            control = false;
                            break;
                        default:
                            System.out.println("  NOT AN OPTION \n");
                            break;
                    }
                }

            } catch (NumberFormatException e) {
                System.out.println("  NOT A NUMBER. Closing application... \n");
                sc.close();
            }

        } catch (IOException e) {
            System.out.println("Error connecting to the server.");
            e.printStackTrace();
        } finally {
            releaseResourcesClient(inputStream, socket);
        }

    }

    private static void releaseResourcesClient(InputStream inputStream, Socket socket) {
        try {
            inputStream.close();
        } catch (IOException ex) {
            Logger.getLogger(MainClient.class.getName()).log(Level.SEVERE, null, ex);
        }

        try {
            socket.close();
        } catch (IOException ex) {
            Logger.getLogger(MainClient.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    //TODO revisar este con el del server
    public static boolean loginPatient()  {
        try {
            System.out.println("Please enter your log in data credentials: ");
            System.out.print("Username: ");
            String username = sc.nextLine();
            System.out.print("Password: ");
            String password = sc.nextLine();
            //sending request to the server
            printWriter.println("LOGIN|" + username + "|" + password);
            System.out.println("Sent login data "+username+ "|"+password);
            //response of the server reading
            String responseServer = bufferedReader.readLine();
            System.out.println("received response");

            if ("LOGIN_SUCCESS".equals(responseServer)) {
                System.out.println("Login successful.");
                return true;
            } else if ("ERROR".equals(responseServer)){
                System.out.println("Login failed.");
                return false;
            }else{
            System.out.println("Unexpected response from server");
            return false;
            }
        }catch(IOException e){
            System.out.println("Error during login "+ e.getMessage());
            return false;
        }
    }

//TODO: podemos usar metodo .trim() para eliminar espacios y comparar bien las cadenas
    //TODO: gestionar excepciones COMPLETAR
    public static void registerPatient() throws IOException {
        System.out.println("Please enter your user credentials for registration:");
        System.out.print("Username: ");
        String username = sc.nextLine();
        System.out.print("Password: ");
        String password = sc.nextLine();

        System.out.println("Please enter patient details:");
        System.out.print("\nName: ");
        String name = sc.nextLine();
        System.out.print("\nSurname: ");
        String surname = sc.nextLine();

        boolean geneticBackground=false;

        while (true) {
            System.out.print("\nGenetic Background (true/false): ");
            String input=sc.nextLine();
            try {
                geneticBackground = Boolean.parseBoolean(input);
                break;
            } catch (Exception e) {
                System.out.println("Invalid input. Please enter 'true' or 'false'.");
            }
        }
        String patientData = "REGISTER_PATIENT|" + username + "|" + password + "|" + name + "|" + surname + "|" + geneticBackground;
        //sending data to the server
        printWriter.println(patientData);
        //response of the server:
        String response = bufferedReader.readLine();
        System.out.println("Patient data sent to the server for registration.");
        if (response != null) {
            System.out.println("Server response: " + response);
        } else {
            System.out.println("No response received from server.");
        }
    }

    private static void openMedicalRecord() {
        // TODO
        System.out.println("Opening medical record...");
    }

    private static void sendMedicalRecord() {
        // TODO
        System.out.println("Sending medical record...");
        //patient.sendMedicalRecord(medicalRecord, socket);
    }
}

