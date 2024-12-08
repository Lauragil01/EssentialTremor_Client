package mainClient;

import pojos.MedicalRecord;
import pojos.Patient;

import java.io.*;
import java.net.Socket;
import java.util.Arrays;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

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
            System.out.println("Error connecting ti the server");
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
                int loggedInOption = sc.nextInt();
                sc.nextLine(); // Limpiar el buffer
                switch (loggedInOption) {
                    case 1:
                        openMedicalRecord();
                        break;
                    case 2:
                        sendMedicalRecord();
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
            System.out.println("Sent login data "+username+ "|"+password);
            //response of the server reading
            String responseServer = bufferedReader.readLine();
            System.out.println("received response");

            if ("SUCCESS".equals(responseServer)) {
                System.out.println("Login successful. Welcome"+username);
                return true;
            } else{
                System.out.println("Login failed."+ responseServer);
                return false;
            }
        }catch(IOException e){
            System.out.println("Error during login "+ e.getMessage());
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

        if ("SUCCESS".equals(response)) {
            System.out.println("Registration successful. You can now log in.");
        } else {
            System.out.println("Registration failed: " + response);
        }
    }

    private static void openMedicalRecord() {
        // TODO
        System.out.println("Opening medical record...");
    }
    private static void sendMedicalRecord() {
            System.out.println("Sending Medical Record");
        try {
            System.out.println("\n=== Creating Medical Record ===");
            MedicalRecord medicalRecord = createMedicalRecord();

            if (medicalRecord != null) {
                patient.sendMedicalRecord(medicalRecord, socket);
                System.out.println("Medical record sent successfully.");
            } else {
                System.out.println("Failed to create medical record.");
            }
        } catch (IOException e) {
            System.out.println("Error sending medical record: " + e.getMessage());
        }
    }


    //TODO : REVISAR!!!! con openMedicalRecord() in Patient class
    private static MedicalRecord createMedicalRecord() {
        try {
            System.out.println("- Enter Age: ");
            int age = sc.nextInt();
            System.out.println("- Enter Weight (kg): ");
            double weight = sc.nextDouble();
            System.out.println("- Enter Height (cm): ");
            int height = sc.nextInt();
            sc.nextLine(); // Limpiar buffer
            System.out.println("- Enter Symptoms (comma-separated): ");
            String symptomsInput = sc.nextLine();

            // Process symptoms input into a list
            List<String> symptoms = Arrays.asList(symptomsInput.split(","));
            symptoms = symptoms.stream().map(String::trim).collect(Collectors.toList());

            System.out.println("- Collecting physiological signals...");
            List<Integer> emgSignal = patient.obtainSignals("EMG");
            List<Integer> accSignal = patient.obtainSignals("ACC");

            // Create MedicalRecord
            MedicalRecord medicalRecord = new MedicalRecord(age, weight, height, symptoms, accSignal, emgSignal);
            System.out.println("Medical record created successfully.");
            return medicalRecord;
        } catch (InputMismatchException e) {
            System.out.println("Invalid input. Please try again.");
            sc.next(); // Limpiar entrada inválida
            return null;
        }
    }

}

