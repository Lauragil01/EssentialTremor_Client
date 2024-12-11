package mainClient;

import pojos.MedicalRecord;
import pojos.Patient;
import pojos.User;

import java.io.*;
import java.net.Socket;
import java.util.InputMismatchException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MainClient {
    private static Scanner sc = new Scanner(System.in);
    private static Socket socket;
    private static PrintWriter printWriter;
    private static BufferedReader bufferedReader;
    private static InputStream inputStream;
    private static Patient patient;
    private static boolean control=true;


    public static void main(String[] args) {
        try {
            socket = new Socket("localhost", 12345);
            printWriter = new PrintWriter(socket.getOutputStream(), true);
            bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            inputStream = socket.getInputStream();

            while (control) {
                showMainMenu();
                try {
                    int option = sc.nextInt();
                    sc.nextLine(); //clean buffer after reading
                    optionMainMenu(option);
                } catch (InputMismatchException e) {
                    System.out.println("Invalid input. Please enter a number.");
                    sc.next();
                }
            }
        } catch (IOException ex) {
            System.out.println("Error connecting to the server");
            ex.printStackTrace();
        } finally {
            releaseResourcesClient(inputStream,socket);
        }
    }
    private static void showMainMenu(){
        System.out.println("\n@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
        System.out.println("@@                                                                  @@");
        System.out.println("@@                 Welcome.                                         @@");
        System.out.println("@@                 1. Register as user                              @@");
        System.out.println("@@                 2. Log in                                        @@");
        System.out.println("@@                 0. Exit                                          @@");
        System.out.println("@@                                                                  @@");
        System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
        System.out.print("\nSelect an option: ");
    }

    private static void optionMainMenu(int option) {
        switch (option) {
            case 1:
                try {
                    registerPatient();
                } catch (IOException e) {
                    System.out.println("Error during registration: " + e.getMessage());
                }
                break;
            case 2:
                boolean loggedIn = loginPatient();
                if (loggedIn) {
                    showLoggedInMenu();
                }else{
                    System.out.println("Returning to main menu");
                }
                break;
            case 0:
                System.out.println("Exiting...");
                control = false;
                break;
            default:
                System.out.println("Invalid option. Please try again.");
                break;
        }
    }

    private static void showLoggedInMenu() {
        boolean loggedInControl = true;
        while (loggedInControl) {
            System.out.println("\n================= Logged In Menu =================");
            System.out.println("1. Open Medical Record");
            System.out.println("2. Send Medical Record");
            System.out.println("0. Log Out");
            System.out.println("==================================================");
            System.out.print("Select an option: ");

            try {
                int optionLogIn = sc.nextInt();
                sc.nextLine(); // Limpiar el buffer
                switch (optionLogIn) {
                    case 1:
                        openMR();
                        break;
                    case 2:
                        sendMR();
                        break;
                    case 0:
                        System.out.println("Logging out...");
                        loggedInControl = false;
                        break;
                    default:
                        System.out.println("Invalid option. Please try again.");
                        break;
                }
            } catch (InputMismatchException e) {
                System.out.println("Invalid input. Please enter a number.");
                sc.next(); // clean input incorrect
            }
        }
    }

    private static void releaseResourcesClient(InputStream inputStream, Socket socket) {
            try {
                inputStream.close();
            } catch (IOException ex) {
                Logger.getLogger(MainClient.class.getName()).log(Level.SEVERE, "Error closing input stream", ex);
            }

            try {
                socket.close();
            } catch (IOException ex) {
                Logger.getLogger(MainClient.class.getName()).log(Level.SEVERE, "Error closing socket", ex);
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
            System.out.println("Sent login data " + username + "|" + password);

            //response of the server reading
            String responseServer = bufferedReader.readLine();
            System.out.println("Received response: " + responseServer);

            if (responseServer.startsWith("SUCCESS|")) {
                //System.out.println("You are now in the application " + username);
                String[] patientData = responseServer.substring(8).split("\\|");

                if (patientData.length >=5) { // Ensure enough data exists
                    String name = patientData[0];
                    String surname = patientData[1];
                    boolean geneticBackground = Boolean.parseBoolean(patientData[2]);
                    String userUsername = patientData[3];
                    String hashedPassword = patientData[4];

                    // Initialize patient object
                    patient = new Patient(name, surname, geneticBackground, new User(username, hashedPassword));
                    return true;

                    //TODO: MIRAR ESTO--> para llamar a openMR()
                    // Initialize patient
                    //patient = new Patient(name, surname, geneticBackground, new User(userUsername, password));
                    //return true;
                } else {
                    System.out.println("Error: Incomplete data received from server.");
                    return false;
                }
            } else {
                System.out.println("Login failed. " + responseServer);
                return false;
            }
        } catch (IOException e) {
            System.out.println("Error during login: " + e.getMessage());
            return false;
        }
    }

//TODO: podemos usar metodo .trim() para eliminar espacios y comparar bien las cadenas
    //TODO: revisar excepciones
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

        boolean geneticBackground;

        while (true) {
            System.out.print("\nGenetic Background (true/false): ");
            try {
                geneticBackground = Boolean.parseBoolean(sc.nextLine());
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

        if (response.startsWith("SUCCESS")) {
            System.out.println("Registration successful. You can now log in.");
            patient = new Patient(name, surname, geneticBackground, new User(username, password));
        } else {
            System.out.println("Registration failed: " + response);
        }
    }



    //TODO: REVISAR!!!!!!!
    private static void openMR() {
        System.out.println("Opening medical record...");
        try {
            MedicalRecord medicalRecord = patient.openMedicalRecord();
            if (medicalRecord != null) {
                System.out.println("Medical Record Created...");
                System.out.println(medicalRecord); //toString() MedicalRecord
               // patient.getMedicalRecords().add(medicalRecord);
            } else {
                System.out.println("Failed to create medical record.");
            }
        } catch (Exception e) {
            System.out.println("Error opening medical record: " + e.getMessage());
        }
    }



    private static void sendMR() {
        try {
            //System.out.println("\n  Sending Medical Record ");

            // Crear el registro médico utilizando Patient
            MedicalRecord medicalRecord =patient.chooseMR();

            if (medicalRecord == null) {
                System.out.println("No medical records available to send.");
                return;
            }

            patient.sendMedicalRecord(medicalRecord, socket);

            // Read the server's response
            String response = bufferedReader.readLine();
            if (response.startsWith("SUCCESS|")) {
                System.out.println("Doctor's response: " + response.substring("SUCCESS|".length()));
            } else {
                System.out.println("Failed to send medical record: " + response);
            }
        } catch (IOException e) {
            System.err.println("Error sending medical record: " + e.getMessage());
        }
    }

}

