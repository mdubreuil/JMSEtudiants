package pkgConso;

import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import javax.annotation.Resource;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.ejb.MessageDrivenContext;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.MessageListener;

/**
 *
 * @author Epulapp
 * 
 */
@MessageDriven(activationConfig = {
    @ActivationConfigProperty(propertyName = "clientId", propertyValue = "Inscription"),
    @ActivationConfigProperty(propertyName = "destinationLookup", propertyValue = "Inscription"),
    @ActivationConfigProperty(propertyName = "subscriptionDurability", propertyValue = "Durable"),
    @ActivationConfigProperty(propertyName = "subscriptionName", propertyValue = "Inscription"),
    @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Topic")
})
public class Inscription implements MessageListener {
    @Resource
    private MessageDrivenContext mdc;
    
    static final Logger logger = Logger.getLogger("LyonMag");
    
    public Inscription() {
        try{
            FileHandler fh=new FileHandler("Inscriptions.log");
            fh.setFormatter(new SimpleFormatter());
            logger.addHandler(fh);
        }catch(Exception ex){}
    }
    
    @Override
    public void onMessage(Message message) {
        MapMessage mapMessage = null;
        String msg;
        try {
            if (message instanceof MapMessage) {
                mapMessage = (MapMessage) message;
                msg = "Numéro de l'étudiant : " + mapMessage.getString("id_etudiant") + "\n";
                msg += "Nom de l'étudiant : " + mapMessage.getString("nom") + "\n";
                msg += "Prénom de l'étudiant : " + mapMessage.getString("prenom") + "\n";
                msg += "Département de l'étudiant : " + mapMessage.getString("id_departement") + "\n";
                logger.log(Level.INFO, "{0}", msg);
            }
        } catch (Exception e) {
            mdc.setRollbackOnly();
            logger.log(Level.INFO, "Erreur sur réception message : {0}", e.getMessage());
        }
    }
    
}
