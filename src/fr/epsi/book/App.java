package fr.epsi.book;

import fr.epsi.book.dal.BookDAO;
import fr.epsi.book.dal.ContactDAO;
import fr.epsi.book.dal.IDAO;
import fr.epsi.book.domain.Book;
import fr.epsi.book.domain.Contact;

import javax.jws.soap.SOAPBinding;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;

public class App {

    private static final String BOOK_BKP_DIR = "./resources/backup/";

    private static final Scanner sc = new Scanner(System.in);
    private static Book book = new Book();
    private static ContactDAO contactDAO = new ContactDAO();
    private static BookDAO bookDAO = new BookDAO();

    public static void main(String... args) {
        dspMainMenu();
    }

    public static Contact.Type getTypeFromKeyboard() {
        int response;
        boolean first = true;
        do {
            if (!first) {
                System.out.println("***********************************************");
                System.out.println("* Mauvais choix, merci de recommencer !       *");
                System.out.println("***********************************************");
            }
            System.out.println("*******Choix type de contact *******");
            System.out.println("* 1 - Perso                        *");
            System.out.println("* 2 - Pro                          *");
            System.out.println("************************************");
            System.out.print("*Votre choix : ");
            try {
                response = sc.nextInt() - 1;
            } catch (InputMismatchException e) {
                response = -1;
            } finally {
                sc.nextLine();
            }
            first = false;
        } while (0 != response && 1 != response);
        return Contact.Type.values()[response];
    }

    public static void addContact() {
        System.out.println("**************************************");
        System.out.println("**********Ajout d'un contact**********");
        Contact contact = new Contact();
        System.out.print("Entrer le nom :");
        contact.setName(sc.nextLine());
        System.out.print("Entrer l'email :");
        contact.setEmail(sc.nextLine());
        System.out.print("Entrer le téléphone :");
        contact.setPhone(sc.nextLine());
        contact.setType(getTypeFromKeyboard());

        try {
            contactDAO.create(contact);
        }catch (SQLException se){
            se.getErrorCode();
        }

        book.addContact(contact);

        try {
            bookDAO.update(book);
        }catch (SQLException se){
            se.getErrorCode();
        }
        System.out.println("Nouveau contact ajouté ...");
    }

    public static void editContact() {
        System.out.println("*********************************************");
        System.out.println("**********Modification d'un contact**********");
        dspContacts(false);
        System.out.print("Entrer l'identifiant du contact : ");
        Long id = sc.nextLong();
        Contact contact = book.getContacts().get(id.toString());
        if (null == contact) {
            System.out.println("Aucun contact trouvé avec cet identifiant ...");
        } else {
            System.out
                    .print("Entrer le nom ('" + contact.getName() + "'; laisser vide pour ne pas mettre à jour) : ");
            String name = sc.nextLine();
            if (!name.isEmpty()) {
                contact.setName(name);
            }
            System.out.print("Entrer l'email ('" + contact
                    .getEmail() + "'; laisser vide pour ne pas mettre à jour) : ");
            String email = sc.nextLine();
            if (!email.isEmpty()) {
                contact.setEmail(email);
            }
            System.out.print("Entrer le téléphone ('" + contact
                    .getPhone() + "'; laisser vide pour ne pas mettre à jour) : ");
            String phone = sc.nextLine();
            if (!phone.isEmpty()) {
                contact.setPhone(phone);
            }

            // Modification en bdd
            try {
                contactDAO.update(contact);
            }catch (SQLException se){
                se.getErrorCode();
            }


            System.out.println("Le contact a bien été modifié ...");
        }
    }

    public static void deleteContact() {
        System.out.println("*********************************************");
        System.out.println("***********Suppression d'un contact**********");
        dspContacts(false);
        System.out.print("Entrer l'identifiant du contact : ");
        Long id = sc.nextLong();

        //Suppression en bdd
        try {
            Contact ctc = contactDAO.findById(id);
            contactDAO.remove(ctc);
        }catch (SQLException se){
            se.getErrorCode();
        }

        Contact contact = book.getContacts().remove(id.toString());
        if (null == contact) {
            System.out.println("Aucun contact trouvé avec cet identifiant ...");
        } else {
            System.out.println("Le contact a bien été supprimé ...");
        }
    }

    public static void sort() {
        int response;
        boolean first = true;
        do {
            if (!first) {
                System.out.println("***********************************************");
                System.out.println("* Mauvais choix, merci de recommencer !       *");
                System.out.println("***********************************************");
            }
            System.out.println("*******Choix du critère*******");
            System.out.println("* 1 - Nom     **              *");
            System.out.println("* 2 - Email **                *");
            System.out.println("*******************************");
            System.out.print("*Votre choix : ");
            try {
                response = sc.nextInt();
            } catch (InputMismatchException e) {
                response = -1;
            } finally {
                sc.nextLine();
            }
            first = false;
        } while (0 >= response || response > 2);
        Map<String, Contact> contacts = book.getContacts();
        switch (response) {
            case 1:
                contacts.entrySet().stream()
                        .sorted((e1, e2) -> e1.getValue().getName().compareToIgnoreCase(e2.getValue().getName()))
                        .forEach(e -> dspContact(e.getValue()));
                break;
            case 2:

                contacts.entrySet().stream().sorted((e1, e2) -> e1.getValue().getEmail()
                        .compareToIgnoreCase(e2.getValue().getEmail()))
                        .forEach(e -> dspContact(e.getValue()));
                break;
        }
    }

    public static void searchContactsByName() {

        System.out.println("*******************************************************************");
        System.out.println("************Recherche de contacts sur le nom ou l'email************");
        System.out.println("*******************************************************************");
        System.out.print("*Mot clé (1 seul) : ");
        String word = sc.nextLine();
        Map<String, Contact> subSet = book.getContacts().entrySet().stream()
                .filter(entry -> entry.getValue().getName().contains(word) || entry
                        .getValue().getEmail().contains(word))
                .collect(HashMap::new, (newMap, entry) -> newMap
                        .put(entry.getKey(), entry.getValue()), Map::putAll);

        if (subSet.size() > 0) {
            System.out.println(subSet.size() + " contact(s) trouvé(s) : ");
            subSet.entrySet().forEach(entry -> dspContact(entry.getValue()));
        } else {
            System.out.println("Aucun contact trouvé avec cet identifiant ...");
        }
    }

    public static void dspContact(Contact contact) {
        System.out.println(contact.getId() + "\t\t\t\t" + contact.getName() + "\t\t\t\t" + contact
                .getEmail() + "\t\t\t\t" + contact.getPhone() + "\t\t\t\t" + contact.getType());
    }

    public static void dspContacts(boolean dspHeader) {
        if (dspHeader) {
            System.out.println("**************************************");
            System.out.println("********Liste de vos contacts*********");
        }
        for (Map.Entry<String, Contact> entry : book.getContacts().entrySet()) {
            dspContact(entry.getValue());
        }
        System.out.println("**************************************");
    }

    public static void dspMainMenu() {
        int response;
        boolean first = true;
        do {
            if (!first) {
                System.out.println("***********************************************");
                System.out.println("* Mauvais choix, merci de recommencer !       *");
                System.out.println("***********************************************");
            }
            System.out.println("**************************************");
            System.out.println("*****************Menu*****************");
            System.out.println("* 1 - Ajouter un contact             *");
            System.out.println("* 2 - Modifier un contact            *");
            System.out.println("* 3 - Supprimer un contact           *");
            System.out.println("* 4 - Lister les contacts            *");
            System.out.println("* 5 - Rechercher un contact          *");
            System.out.println("* 6 - Trier les contacts             *");
            System.out.println("* 7 - Sauvegarder                    *");
            System.out.println("* 8 - Restaurer                      *");
            System.out.println("* 9 - Export des contacts            *");
            System.out.println("* 10 - Export des contacts en csv    *");
            System.out.println("* 11 - Quitter                       *");
            System.out.println("**************************************");
            System.out.print("*Votre choix : ");
            try {
                response = sc.nextInt();
            } catch (InputMismatchException e) {
                response = -1;
            } finally {
                sc.nextLine();
            }
            first = false;
        } while (1 > response || 10 < response);
        switch (response) {
            case 1:
                addContact();
                dspMainMenu();
                break;
            case 2:
                editContact();
                dspMainMenu();
                break;
            case 3:
                deleteContact();
                dspMainMenu();
                break;
            case 4:
                dspContacts(true);
                dspMainMenu();
                break;
            case 5:
                searchContactsByName();
                dspMainMenu();
                break;
            case 6:
                sort();
                dspMainMenu();
                break;
            case 7:
                storeContacts();
                dspMainMenu();
                break;
            case 8:
                restoreContacts();
                dspMainMenu();
                break;
            case 9:
                exportContacts();
                dspMainMenu();
                break;
            case 10:
                exportContactsToCsv();
                dspMainMenu();
                break;
        }
    }

    private static void storeContacts() {

        Path path = Paths.get(BOOK_BKP_DIR);
        if (!Files.isDirectory(path)) {
            try {
                Files.createDirectory(path);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        String backupFileName = new SimpleDateFormat("yyyy-MM-dd-hh-mm-ss").format(new Date()) + ".ser";
        try (ObjectOutputStream oos = new ObjectOutputStream(Files
                .newOutputStream(Paths.get(BOOK_BKP_DIR + backupFileName)))) {
            oos.writeObject(book);
            System.out.println("Sauvegarde terminée : fichier " + backupFileName);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void restoreContacts() {

        try (DirectoryStream<Path> ds = Files.newDirectoryStream(Paths.get(BOOK_BKP_DIR), "*.ser")) {
            List<Path> paths = new ArrayList<>();
            System.out.println("**************************************");
            System.out.println("********Choix d'une sauvegarde********");

            for (Path path : ds) {

                paths.add(path);
                System.out.println(paths.size()+ " - " + path.getFileName());
            }

            System.out.println("**************************************");
            System.out.print("*Choisir une sauvegarde à restaurer : ");

            int selectedNumber =sc.nextInt();

            if (selectedNumber > 0 && selectedNumber < paths.size()) {
                System.out.println();

                Path selectedPath = paths.get(selectedNumber - 1);
                System.out.println("Restauration du fichier : " + selectedPath.getFileName());
                try (ObjectInputStream ois = new ObjectInputStream(Files.newInputStream(selectedPath))) {
                    book = (Book) ois.readObject();
                    System.out.println("Restauration terminée : fichier " + selectedPath.getFileName());

                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }else{
                System.out.println(" Nombre incorrect ");
                restoreContacts();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void exportContacts() {

        try {
            JAXBContext context = JAXBContext.newInstance(Book.class);
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            marshaller.marshal(book, System.out);
            marshaller.marshal(book, System.out);
        } catch (JAXBException e) {
            e.printStackTrace();
        }
    }

    private static void exportContactsToCsv() {
        String csv = "Nom;Email;Telephone;Type de contact \n";

        try {
           List<Book> books = bookDAO.findAll();
           for (Book book: books){
               Map<String, Contact> contacts = book.getContacts();
               for (Map.Entry<String, Contact> entry : contacts.entrySet()) {
                   String newLine = entry.getValue().getName() + ";" +
                           entry.getValue().getEmail() + ";" +
                           entry.getValue().getPhone() + ";" +
                           entry.getValue().getType() + "\n";

                   csv = csv.concat(newLine);
               }
           }

        }catch(SQLException se){
            se.getErrorCode();
        }
        // V1 sans base de données
        /*Map<String, Contact> contacts = book.getContacts();
        for (Map.Entry<String, Contact> entry : contacts.entrySet()) {
            String newLine = entry.getValue().getName() + ";" +
                    entry.getValue().getEmail() + ";" +
                    entry.getValue().getPhone() + ";" +
                    entry.getValue().getType() + "\n";

            csv = csv.concat(newLine);
        }*/
        try {
            System.out.print("Entrer le nom pour le fichier : ");
            String FileName = sc.nextLine() + ".csv";
            FileOutputStream fos = new FileOutputStream(FileName);
            fos.write(csv.getBytes());
            fos.flush();
            fos.close();
            System.out.println(" Enregistrement du fichier " + FileName);

        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
