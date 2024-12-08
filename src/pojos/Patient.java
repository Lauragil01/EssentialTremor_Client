package pojos;

import bITalino.BITalino;
import bITalino.BITalinoException;
import bITalino.BitalinoDemo;
import bITalino.Frame;
import signals.ACC;
import signals.Signals;
import signals.EMG;

import javax.bluetooth.RemoteDevice;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class Patient {
    private String name;
    private String surname;
    private Boolean genetic_background;
    //private User user;
    private List<MedicalRecord> medicalRecords;
    //private List<Doctor> doctors;

    public Patient() {
    }

    public Patient(String name, String surname, Boolean genBack) {
        this.name = name;
        this.surname = surname;
        this.genetic_background = genBack;
        this.medicalRecords = new ArrayList<MedicalRecord>();
        //this.doctors = new ArrayList<Doctor>();
    }

    public void setMedicalRecords(List<MedicalRecord> medicalRecords) {
        this.medicalRecords = medicalRecords;
    }

    /*public List<Doctor> getDoctors() {
        return doctors;
    }

    public void setDoctors(List<Doctor> doctors) {
        this.doctors = doctors;
    }*/

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    /*public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }*/

    public List<MedicalRecord> getMedicalRecords() {
        return medicalRecords;
    }

    public void setMedicalRecord(List<MedicalRecord> medicalRecords) {
        this.medicalRecords = medicalRecords;
    }

    @Override
    public String toString() {
        return "- Name: " + name + '\'' +
                "- Surname: " + surname + '\'';
        //"- State: " + state +
        //"- Treatment: " + treatment;
    }

    private void openMedicalRecord() {
        MedicalRecord record=askData();
        record.setPatientName(this.name);
        record.setPatientSurname(this.surname);
        record.setGenetic_background(this.genetic_background);
        Signals signals = obtainSignals();
        record.setAcceleration(signals.getAcc());
        record.setEmg(signals.getEmg());
        this.getMedicalRecords().add(record);
    }

    //Data that the patient fills when creating medicalRecord
    private MedicalRecord askData() {

        Scanner sc = new Scanner(System.in);
        System.out.println("- Age: ");
        int age = sc.nextInt();
        System.out.println("- Weight (kg): ");
        double weight = sc.nextDouble();
        System.out.println("- Height (cm): ");
        int height = sc.nextInt();
        System.out.println("- Symptoms (enter symptoms separated by commas): ");
        String symptomsInput = sc.nextLine();

        //Takes the symptoms input and creates a List
        List<String> symptoms = Arrays.asList(symptomsInput.split(","));
        symptoms = symptoms.stream().map(String::trim).collect(Collectors.toList()); // Trim extra spaces
        sc.close();
        return new MedicalRecord(age, weight, height, symptoms);
    }

    private Signals obtainSignals() {
        Frame[] frame;
        Signals signals = null;
        BITalino bitalino = null;
        try {
            bitalino = new BITalino();
            // Code to find Devices
            //Only works on some OS
            Vector<RemoteDevice> devices = bitalino.findDevices();
            System.out.println(devices);

            //You need TO CHANGE THE MAC ADDRESS
            //You should have the MAC ADDRESS in a sticker in the Bitalino
            String macAddress = "20:17:11:20:51:27";

            //Sampling rate, should be 10, 100 or 1000
            int SamplingRate = 100;
            bitalino.open(macAddress, SamplingRate);

            //0--1= EMG
            //5--6=ACC
            int[] channelsToAcquire = {0, 5};
            bitalino.start(channelsToAcquire);

            //Objects EMG and ACC
            ACC acc = new ACC();
            EMG emg = new EMG();

            //Read in total 10000000 times
            for (int j = 0; j < 100; j++) {

                //Each time read a block of 10 samples
                int block_size = 10;
                frame = bitalino.read(block_size);

                System.out.println("size block: " + frame.length);

                //Print the samples
                for (int i = 0; i < frame.length; i++) {

                    acc.getTimestamp().add(j * block_size + i);
                    emg.getTimestamp().add(j * block_size + i);

                    emg.getSignalData().add(frame[i].analog[0]);
                    acc.getSignalData().add(frame[i].analog[1]);
                    System.out.println((j * block_size + i)//Time
                                    + " seq: " + frame[i].seq + " "
                                    + frame[i].analog[0] + " "//EMG
                                    + frame[i].analog[1] + " "//ACC
                            //  + frame[i].analog[2] + " "
                            //  + frame[i].analog[3] + " "
                            //  + frame[i].analog[4] + " "
                            //  + frame[i].analog[5]
                    );

                }
            }
            //stop acquisition
            bitalino.stop();

            /*System.out.println(emg.getTimestamp());
            System.out.println(emg.getSignalData());
            System.out.println(acc.getSignalData());*/
            signals = new Signals(acc, emg);
            saveDataToFile(this.getName(), acc, emg); //save info in a txt file

        } catch (BITalinoException ex) {
            Logger.getLogger(BitalinoDemo.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Throwable ex) {
            Logger.getLogger(BitalinoDemo.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                //close bluetooth connection
                if (bitalino != null) {
                    bitalino.close();
                }
            } catch (BITalinoException ex) {
                Logger.getLogger(BitalinoDemo.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return signals;
    }


    private void saveDataToFile(String patientName, ACC acc, EMG emg) {
        LocalDateTime moment = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
        String timestamp = moment.format(formatter);

        Path folderPath = Paths.get("BITalinoData");
        try {
            Files.createDirectories(folderPath);
        } catch (IOException e) {
            Logger.getLogger(BitalinoDemo.class.getName()).log(Level.SEVERE, "Error creating directory", e);
        }

        String fileName = "BITalinoData/" + patientName + "_" + timestamp + ".txt";
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
            writer.write("Patient Name: " + patientName + "\n");
            writer.write("Date and Time: " + moment + "\n\n");

            writer.write("EMG Data:\n");
            for (int i = 0; i < emg.getSignalData().size(); i++) {
                writer.write("Time: " + emg.getTimestamp().get(i) + ", Signal: " + emg.getSignalData().get(i) + "\n");
            }

            writer.write("\nACC Data:\n");
            for (int i = 0; i < acc.getSignalData().size(); i++) {
                writer.write("Time: " + acc.getTimestamp().get(i) + ", Signal: " + acc.getSignalData().get(i) + "\n");
            }

            System.out.println("Data saved to " + fileName);
        } catch (IOException e) {
            Logger.getLogger(BitalinoDemo.class.getName()).log(Level.SEVERE, "Error writing file", e);
        }
    }


    //choosing Medical Record
    private MedicalRecord chooseMR() {
        if (this.medicalRecords.isEmpty()) {
            System.out.println("No medical records available.");
            return null;
        }

        Scanner sc = new Scanner(System.in);
        System.out.println("Available Medical Records:");

        // Display medical records with indices
        for (int i = 0; i < this.medicalRecords.size(); i++) {
            System.out.println((i + 1) + ": " + this.medicalRecords.get(i)); // using toString of MedicalRecord class
        }

        int choice;
        while (true) {
            try {
                System.out.print("Enter the number of the medical record you want to choose: ");
                choice = sc.nextInt();
                if (choice > 0 && choice <= medicalRecords.size()) {
                    break;
                } else {
                    System.out.println("Invalid choice. Please enter a number between 1 and " + medicalRecords.size());
                }
            } catch (InputMismatchException e) {
                System.out.println("Invalid input. Please enter a numeric value.");
                sc.next(); // Clear the invalid input
            }
        }
        sc.close();
        // Return the selected medical record
        return medicalRecords.get(choice-1);
    }

    //TODO: CHECK info MR sent to server
    public void sendMedicalRecord(MedicalRecord medicalRecord, Socket socket) throws IOException {
            PrintWriter printWriter = new PrintWriter(socket.getOutputStream(), true);

            // Collection of fields into a list
            List<String> fields = new ArrayList<>();
            fields.add(medicalRecord.getPatientName());
            fields.add(medicalRecord.getPatientSurname());
            fields.add(String.valueOf(medicalRecord.getAge()));
            fields.add(String.valueOf(medicalRecord.getWeight()));
            fields.add(String.valueOf(medicalRecord.getHeight()));

            // Add symptoms
            fields.add(joinWithCommas(medicalRecord.getSymptoms()));

            // Add signal data
            fields.add(joinIntegersWithCommas(medicalRecord.getAcceleration().getTimestamp()));
            fields.add(joinIntegersWithCommas(medicalRecord.getAcceleration().getSignalData()));
            fields.add(joinIntegersWithCommas(medicalRecord.getEmg().getSignalData()));

            // Add genetic background
            fields.add(String.valueOf(medicalRecord.getGenetic_background()));

            // Serializing and sending the record
            String recordString = joinWithCommas(fields);
            printWriter.println(recordString);
            System.out.println("Medical record sent successfully: " + recordString);

    }


    public static String joinWithCommas(List<String> list) {
        return String.join(",", list);
    }

    public static String joinIntegersWithCommas(List<Integer> list) {
        return list.stream().map(String::valueOf) // Convert Integer to String
                .collect(Collectors.joining(",")); // commas
    }

    private DoctorsNote receiveDoctorsNote(MedicalRecord medicalRecord) throws IOException {
        //TODO check this one
        DoctorsNote doctorsNote = null;
        try (ServerSocket serverSocket = new ServerSocket(9009)) {  // Puerto 9009 para coincidir con el cliente
            System.out.println("Server started, waiting for the Doctor Notes...");

            try (Socket socket = serverSocket.accept();
                 BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

                System.out.println("Client connected. Receiving doctor's note...");

                // Read/ print each line doctor's data
                String doctorName = bufferedReader.readLine();
                System.out.println(doctorName);
                String doctorSurname = bufferedReader.readLine();
                System.out.println(doctorSurname);
                String notes = bufferedReader.readLine();
                System.out.println(notes);

                releaseReceivingResources(bufferedReader, socket, serverSocket);

                doctorsNote = new DoctorsNote(doctorName, doctorSurname, notes);
                medicalRecord.getDoctorsNotes().add(doctorsNote);
                //TODO this is in the main
                //DoctorsNote doctorsNote = createDoctorsNote(medicalRecord);

                return doctorsNote;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return doctorsNote;
    }


    //patient chooses what record to see
    private void seeDoctorNotes(MedicalRecord medicalRecord) {

        List<DoctorsNote> notes=medicalRecord.getDoctorsNotes();

        if(notes.isEmpty()){
            System.out.println("There is not doctor notes available for this Medical Record.");
            return;
        }
        System.out.println("Doctor notes in this medical record");
        for(int i=0; i<notes.size();i++){
            System.out.println((i + 1)+ ": "+notes.get(i).getNotes());
        }
    }

    private static void releaseReceivingResources(BufferedReader bufferedReader,
                                                  Socket socket, ServerSocket socketServidor) {
        try {
            bufferedReader.close();
        } catch (IOException ex) {
            Logger.getLogger(Doctor.class.getName()).log(Level.SEVERE, null, ex);
        }

        try {
            socket.close();
        } catch (IOException ex) {
            Logger.getLogger(Doctor.class.getName()).log(Level.SEVERE, null, ex);
        }

        try {
            socketServidor.close();
        } catch (IOException ex) {
            Logger.getLogger(Doctor.class.getName()).log(Level.SEVERE, null, ex);
        }
    }


//-----------------------TESTS-----------------------------
    /*public static void main(String[] args) throws IOException {
        Patient p = new Patient("a", "a", Boolean.TRUE);
        p.openRecord();
        /*for (int i=0; i<p.getMedicalRecords().size();i++){
            System.out.println(p.getMedicalRecords().get(i));
        }
        MedicalRecord mr = p.chooseMR();
        p.sendMedicalRecord(mr);
    }*/


   /* public static void main(String[] args) {
        // Start a mock server in a new thread
        new Thread(() -> startMockServer()).start();

        // Delay waiting to the Server
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Test the sendMedicalRecord method
        try (Socket socket = new Socket("localhost", 9000)) {
            // Create  MedicalRecord data
            MedicalRecord record = new MedicalRecord(
                    "Ana", "Losada", 23, 60, 170,
                    Arrays.asList("Tremor", "Fatigue"),
                    new ACC(Arrays.asList(1, 2, 3), Arrays.asList(10, 20, 30)),
                    new EMG(Arrays.asList(4, 5, 6), Arrays.asList(10, 20, 30)),
                    true
            );

            //Patient instance and test the method
            Patient patient = new Patient("Ana", "Losada", true);
            patient.sendMedicalRecord(record, socket);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Mock server to test the function
    private static void startMockServer() {
        try (ServerSocket serverSocket = new ServerSocket(9000)) {
            System.out.println("Mock server started, waiting for connection...");
            try (Socket clientSocket = serverSocket.accept();
                 BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))) {

                System.out.println("Client connected. Receiving data...");
                String receivedData;
                while ((receivedData = reader.readLine()) != null) {
                    System.out.println("Received: " + receivedData);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();

        }
    }*/
}
