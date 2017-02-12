import javax.annotation.Resource;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;

/**
 *
 * @author Mélanie DUBREUIL 4APP
 * @author Ophélie EOUZAN 4APP
 * 
 */

public class Recepteur {
    @Resource(mappedName = "FabriqueConnexionJMS")
    private static ConnectionFactory fabriqueConnexionJMS;
    @Resource(mappedName = "Confirmation")
    private static Queue fileAttenteJMS;
    private static Connection connection = null;
    private static Session session = null;
    private static MessageConsumer consommateur = null;
    private static Message message = null;
    private static TextMessage textMessage = null;

    public static void main(String[] args) {
        try {
            connection = fabriqueConnexionJMS.createConnection();
            session = connection.createSession(false, 0);
            consommateur = session.createConsumer(fileAttenteJMS);
            connection.start();
            message = consommateur.receive(1000);
            
            while (message != null) {
                if (message instanceof TextMessage){
                    textMessage = (TextMessage)message;
                    System.out.println("Inscription reçue : " + textMessage.getText());
                }
                message = consommateur.receive(1000);
            }
            
        } catch(Exception ex){
            System.out.println("Erreur : " + ex.getMessage());
        } finally {
            if (connection != null)
            try{
            connection.close();
            }catch(Exception e){}
        }
    }    
}
