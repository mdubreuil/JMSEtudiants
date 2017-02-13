package pkgConso;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import javax.annotation.Resource;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.ejb.MessageDrivenContext;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

/**
 *
 * @author Mélanie DUBREUIL 4APP
 * @author Ophélie EOUZAN 4APP
 * 
 */
@MessageDriven(activationConfig = {
    @ActivationConfigProperty(propertyName = "clientId", propertyValue = "MdbInscription"),
    @ActivationConfigProperty(propertyName = "destinationLookup", propertyValue = "InscriptionTopic"),
    @ActivationConfigProperty(propertyName = "subscriptionDurability", propertyValue = "Durable"),
    @ActivationConfigProperty(propertyName = "subscriptionName", propertyValue = "MdbInscription"),
    @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Topic")
})
public class MdbInscription implements MessageListener {
    @Resource
    private MessageDrivenContext mdc;
    
    static final Logger logger = Logger.getLogger("Inscriptions");
    
    // Variables permettant d'insérer les inscriptions en BD
    @Resource(mappedName = "jdbc/Inscription1")
    private DataSource dataSource1;
    @Resource(mappedName = "jdbc/Inscription2")
    private DataSource dataSource2;
    private PreparedStatement ps = null;
    private java.sql.Connection c = null;
    
    // Statut des transactions dans les bases inscription1 et inscription2
    private boolean ajoutBd1;
    private boolean ajoutBd2;
    
    public MdbInscription() {
        try{
            FileHandler fh=new FileHandler("Inscriptions.log");
            fh.setFormatter(new SimpleFormatter());
            logger.addHandler(fh);
        } catch(IOException | SecurityException ex){}
    }
    
    @Override
    public void onMessage(Message message) {
        MapMessage mapMessage = null;
        String msg;
        try {
            if (message instanceof MapMessage) {
                logger.log(Level.INFO, "{0}", "Recieved MapMessage");
                
                // Récupération des informations saisies par l'application Secretariat via objet MapMessage
                mapMessage = (MapMessage) message;             
                msg = "Numéro de l'étudiant : " + mapMessage.getString("id_etudiant") + "\n";
                msg += "Nom de l'étudiant : " + mapMessage.getString("nom") + "\n";
                msg += "Prénom de l'étudiant : " + mapMessage.getString("prenom") + "\n";
                msg += "Département de l'étudiant : " + mapMessage.getString("id_departement") + "\n";
                logger.log(Level.INFO, "{0}", msg);
                
                // Ajout des inscriptions dans les bases inscription1 et inscription2
                ajoutBd1(mapMessage.getString("id_etudiant"),mapMessage.getString("nom"),mapMessage.getString("prenom"),mapMessage.getString("id_departement"));
                ajoutBd2(mapMessage.getString("id_etudiant"),mapMessage.getString("nom"),mapMessage.getString("prenom"),mapMessage.getString("id_departement"));
                
                // Ecriture du message dans les logs du MDB
                logger.log(Level.INFO, "{0}", "Insertion BDD effetuée avec ou sans erreur.");
                
                // Acquittement de l'inscription pour l'application DirecteurEtudes
                confirmerTransactionBd(mapMessage);
            }
        } catch (Exception e) {
            mdc.setRollbackOnly();
            logger.log(Level.INFO, "Erreur sur réception message : {0}", e.getMessage());
        } finally {
            try {
                if (c != null) c.close();
            } catch(Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    public void ajoutBd1 (String id_etudiant,String nom, String prenom, String id_departement){
        try {
            c = dataSource1.getConnection("inscription1", "inscription1");            
            String requete = "insert into etudiant (id_etudiant, nom, prenom, id_departement)";
            requete += " values (?,?,?,?)";            
            ps = c.prepareStatement(requete);
            ps.setInt(1,Integer.valueOf(id_etudiant));
            ps.setString(2,nom);
            ps.setString(3,prenom);
            ps.setInt(4,Integer.valueOf(id_departement));
            ps.executeUpdate();
            ajoutBd1 = true;
        } catch (SQLException ex) {
            ajoutBd1 = false;
            Logger.getLogger(MdbInscription.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                if (ps != null) ps.close();
                if (c != null) c.close();
            } catch(Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    public void ajoutBd2 (String id_etudiant,String nom, String prenom, String id_departement){
        try {
            c = dataSource2.getConnection("inscription2", "inscription2");            
            String requete = "insert into etudiant (id_etudiant, nom, prenom, id_departement)";
            requete += " values (?,?,?,?)";
            ps = c.prepareStatement(requete);
            ps.setInt(1,Integer.valueOf(id_etudiant));
            ps.setString(2,nom);
            ps.setString(3,prenom);
            ps.setInt(4,Integer.valueOf(id_departement));
            ps.executeUpdate();
            ajoutBd2 = true;
        } catch (SQLException ex) {
            ajoutBd2 = false;
            Logger.getLogger(MdbInscription.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                if (ps != null) ps.close();
                if (c != null) c.close();
            } catch(Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    private void confirmerTransactionBd(MapMessage message){
        TextMessage acquittement = null;
        ConnectionFactory fabriqueConnexionJMS;
        InitialContext ctx;
        Connection connection = null;
        String enregistrementBd1 = "", enregistrementBd2 = "";
        
        try {
            ctx = new InitialContext();
            fabriqueConnexionJMS = (ConnectionFactory)ctx.lookup("FabriqueConnexionJMS");
            connection = fabriqueConnexionJMS.createConnection();
            Session session = connection.createSession(false, 0);
            MessageProducer producteur = session.createProducer(message.getJMSReplyTo());
            acquittement = session.createTextMessage();
            // Modification du statut à envoyer dans le message d'acquittement
            if(ajoutBd1){
                enregistrementBd1 = "Succès";
            } else {
                enregistrementBd1 = "Echec";
            }            
            if(ajoutBd2){
                enregistrementBd2 = "Succès";
            } else {
                enregistrementBd2 = "Echec";
            }            
            acquittement.setText("\n Inscription 1 | " + message.getString("nom") + " | " + message.getString("prenom") + " | " + enregistrementBd1 + "\n" +
                                 "Inscription 2 | " + message.getString("nom") + " | " + message.getString("prenom") + " | " + enregistrementBd2 );
            producteur.send(acquittement);
            
        } catch (NamingException | JMSException e) {
            logger.log(Level.INFO, "Erreur sur acquittement : {0}", e.getMessage());
        }finally{
            if (connection != null)
                try{
                    connection.close();
                }catch(Exception e){}
        }
    }
}
