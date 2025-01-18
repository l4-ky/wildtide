package com.example.wildtide;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;
import java.util.ArrayList;

public class Lockey_Manager {
    private static String filesPath="files/";

    public static void initStartup() {
        File dir=new File(filesPath);
        if (!dir.exists()) {
            if (!dir.mkdir()) {
                System.exit(1);
            }
        }
        System.out.println("[Files directory created.]");
    }

    public static boolean doFilesExistFor(String username) throws ClassNotFoundException, IOException {
        File pinFile=new File(filesPath+username+"Pin.bin");
        File contentFile=new File(filesPath+username+"Content.bin");
        return pinFile.exists() && contentFile.exists();
    }

    public static void createFilesFor(String username, String pin) throws IOException {
        File pinFile=new File(filesPath+username+"Pin.bin");
        File contentFile=new File(filesPath+username+"Content.bin");
        pinFile.createNewFile();
        contentFile.createNewFile();
        writePinOf(username, pin);
    }

    public static boolean updateProfile(String oldUsername, String oldPin, String newUsername, String newPin) throws ClassNotFoundException, IOException {
        if (!validAccess(oldUsername, oldPin)) return false;
        if (!oldUsername.equals(newUsername)) {
            if (doFilesExistFor(newUsername)) {
                return false;
            } else {
                File oldContentFile=new File(filesPath+oldUsername+"Content.bin");
                File newContentFile=new File(filesPath+newUsername+"Content.bin");
                oldContentFile.renameTo(newContentFile);
                File oldPinFile=new File(filesPath+oldUsername+"Pin.bin");
                File newPinFile=new File(filesPath+newUsername+"Pin.bin");
                oldPinFile.renameTo(newPinFile);
            }
        }
        if (!oldPin.equals(newPin)) {
            writePinOf(newUsername, newPin);
        }
        return true;
    }

    private static String readPinOf(String username) throws IOException, ClassNotFoundException {
        FileInputStream fileInputStream = new FileInputStream(filesPath+username+"Pin.bin");
        ObjectInputStream objInStream=new ObjectInputStream(fileInputStream);
        String pin=(String)objInStream.readObject();
        fileInputStream.close();
        objInStream.close();
        return pin;
    }

    public static boolean validAccess(String username, String pin) throws ClassNotFoundException, IOException {
        return pin.equals(readPinOf(username));
    }

    public static ArrayList<Lockey_Credentials> readContentOf(String username, String pin) throws IOException, ClassNotFoundException {
        if (!validAccess(username, pin)) throw new RuntimeException("Pin is not correct. Files read permission denied.");
        ArrayList<Lockey_Credentials> credentials=new ArrayList<Lockey_Credentials>();
        //TEST
        File test = new File(filesPath+username+"Content.bin");
        if (test.length()==0) {
            System.out.println("[Empty file for "+username+"]");
        } else {
            FileInputStream fileInputStream = new FileInputStream(filesPath+username+"Content.bin");
            ObjectInputStream objInStream=null;
            try {
                objInStream=new ObjectInputStream(fileInputStream);
            } catch (StreamCorruptedException e) {
                fileInputStream.close();
                throw e;
            } catch (EOFException e) {
                e.printStackTrace();
                fileInputStream.close();
                return new ArrayList<Lockey_Credentials>();
            }
            boolean flag=true;
            while (flag) {
                try {
                    credentials.add((Lockey_Credentials)objInStream.readObject());
                } catch (EOFException e) {
                    flag=false;
                }
            }
            fileInputStream.close();
            objInStream.close();
        }
        return credentials;
    }

    private static void writePinOf(String username, String pin) throws IOException {
        FileOutputStream fileOutputStream = new FileOutputStream(filesPath+username+"Pin.bin");
        ObjectOutputStream objOutStream=new ObjectOutputStream(fileOutputStream);
        objOutStream.writeObject(pin);
        fileOutputStream.close();
        objOutStream.close();
    }

    private static void writeContentOf(String username, ArrayList<Lockey_Credentials> list) throws IOException {
        FileOutputStream fileOutputStream = new FileOutputStream(filesPath+username+"Content.bin", false);
        ObjectOutputStream objOutStream=new ObjectOutputStream(fileOutputStream);
        for (Lockey_Credentials item:list) {
            objOutStream.writeObject(new Lockey_Credentials(item.getName(), item.getTopFields(), item.getBottomFields(), item.isPinned()));
        }
        fileOutputStream.close();
        objOutStream.close();
    }

    public static void store(String username, String pin, Lockey_Credentials newContent) throws IOException, ClassNotFoundException {
        if (!validAccess(username, pin)) throw new RuntimeException("Pin is not correct. Files storing permission denied.");
        ArrayList<Lockey_Credentials> originalContent=readContentOf(username, pin);
        int index=indexOf(originalContent, newContent);
        if (index!=-1) {
            originalContent.set(index, newContent);
        } else {
            originalContent.add(newContent);
        }
        writeContentOf(username, originalContent);
    }

    private static int indexOf(ArrayList<Lockey_Credentials> list, Lockey_Credentials cred) {
        for (Lockey_Credentials elem:list) {
            if (elem.getName().equals(cred.getName())) {
                return list.indexOf(elem);
            }
        }
        return -1;
    }
    
    public static void delete(String username, String pin, String name) throws ClassNotFoundException, IOException {
        if (!validAccess(username, pin)) throw new RuntimeException("Pin is not correct. Files deleting permission denied.");
        ArrayList<Lockey_Credentials> originalContent=readContentOf(username, pin);
        for (Lockey_Credentials act:originalContent) {
            if (act.getName().equals(name)) {
                originalContent.remove(act);
                break;
            }
        }
        writeContentOf(username, originalContent);
    }
}
