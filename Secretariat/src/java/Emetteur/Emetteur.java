package Emetteur;

import javax.annotation.Resource;
import javax.jms.ConnectionFactory;
import javax.jms.MapMessage;
import javax.jms.Queue;
import javax.jms.Topic;
import javax.jms.TopicConnection;
import javax.jms.TopicPublisher;
import javax.jms.TopicSession;

/**
 *
 * @author Mélanie DUBREUIL 4APP
 * @author Ophélie EOUZAN 4APP
 * 
 */
public class Emetteur {
    @Resource(mappedName = "Confirmation")
    private static Queue confirmation;
    @Resource(mappedName = "FabriqueConnexionJMS")
    private static ConnectionFactory fabriqueConnexionJMS;
    @Resource(mappedName = "Inscription")   
    
    private static Topic inscriptions;
    private static TopicConnection connection = null;
    private static TopicSession session = null;
    private static TopicPublisher producteur = null;
    private static MapMessage mapMessage = null;
    
    public static void main(String[] args) {
        String id_etudiant, nom, prenom, id_departement;

        try {
            connection = (TopicConnection)fabriqueConnexionJMS.createConnection();
            session = connection.createTopicSession(false,0);
            producteur = session.createPublisher(inscriptions);
            mapMessage = session.createMapMessage();
            System.out.println("Identifiant de l'étudiant : ");
            id_etudiant = Saisie.lectureChaine();

            while (id_etudiant.compareTo("") > 0){
                mapMessage.setString("id_etudiant", id_etudiant);
                System.out.println("Nom de l'étudiant : ");
                nom = Saisie.lectureChaine();
                mapMessage.setString("nom", nom);
                System.out.println("Prénom de l'étudiant : ");
                prenom = Saisie.lectureChaine();
                mapMessage.setString("prenom", prenom);
                System.out.println("Département de l'étudiant : ");
                id_departement = Saisie.lectureChaine();
                mapMessage.setString("id_departement", id_departement);                
                mapMessage.setJMSReplyTo(confirmation);
                producteur.publish(mapMessage);
                System.out.println("Identifiant de l'étudiant (Vide pour arrêter) : ");
                id_etudiant = Saisie.lectureChaine();
            }
            producteur.close();
        }catch(Exception ex){
            System.out.println("Erreur : " + ex.getMessage());
        }finally{
            if (connection != null)
                try{
                    connection.close();
                }catch(Exception e){}
        }
    }
}
