package final_test;

import java.util.Properties;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueReceiver;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.naming.*;


public class SendHandler {
	    
	private static Session session;
    private static Destination request_dest;
	private static Destination reply_dest;
	
	public void sendQueueMessage(String serverUrl, String userName, String password, String request_queue, String reply_queue, final Long timeout, String messageStr) {

//		Properties props = new Properties();
		Connection connection = null;
		InitialContext jndiContext;
        TextMessage msg;
        ConnectionFactory factory;
        
        System.out.println("Publishing to queue '" + request_queue+ "'");
        
        /*
         * 如果你這邊jndi的property沒有給帳密，你的log那裏會多出anonymous@p10354531的log，
         * jndi的作用是他提供一個中介的服務，他上面可能有10台EMS server，你去找他的某一台server提供服務，
         * 但因為現在我是使用自己的電腦作為server，所以直接連就好，
         * 相關的設定程式碼可以參照"C:\tibco\ems\8.5\samples\java\tibjmsMsgProducer.java"
         */
        
//      props.put(Context.INITIAL_CONTEXT_FACTORY, "com.tibco.tibjms.naming.TibjmsInitialContextFactory");
//		props.put(Context.PROVIDER_URL, "tibjmsnaming://localhost:7222");
		      
        try {		    
//        	//jndi lookup就像是DNS lookup，去找我們的這台Server
//        	jndiContext = new InitialContext(props);
//        	
//        	//建立與EMS之間的connection
//        	factory = (ConnectionFactory)jndiContext.lookup("ConnectionFactory");
        	factory = new com.tibco.tibjms.TibjmsConnectionFactory(serverUrl);
        	connection = factory.createConnection(userName, password);
        	
        	//start a connection
        	connection.start();
        	
        	//建立sesion
        	session = connection.createSession(false, javax.jms.Session.AUTO_ACKNOWLEDGE);
        	
//        	//去前面找到的這台EMS server裡面，根據輸入的名字找queue
//        	request_dest = (javax.jms.Queue)jndiContext.lookup(request_queue);
//        	
//        	// 消息回覆到這個Queue
//        	reply_dest = (javax.jms.Queue)jndiContext.lookup(reply_queue);
        	
        	request_dest = session.createQueue(request_queue);
        	reply_dest = session.createQueue(reply_queue);
        	
        	// 創建一個消息，並設置它的JMSReplyTo爲replyQueue。
        	msg = session.createTextMessage();
		    msg.setText(messageStr);//設定要傳的訊息
		    msg.setJMSReplyTo(reply_dest);

		    System.out.println("Published message: \n" + messageStr);
    		
    		MessageProducer producer = session.createProducer(request_dest);
    		producer.send(msg);//將訊息傳出
    				
    		// 消息的接收者
//    		MessageConsumer comsumer_sendProcess = session.createConsumer(request_dest);
//    		comsumer_sendProcess.setMessageListener(new MessageListener() {
//    			public void onMessage(Message m) {
//    				try {
//    					System.out.println(((TextMessage) m).getText());
//
//    					// 在Reply Queue創建一個新的MessageProducer來回覆消息。
//    					MessageProducer producer = session.createProducer(m.getJMSReplyTo());
//    					Message replyMessage = session.createTextMessage("Got it!");
//    					replyMessage.setJMSCorrelationID(m.getJMSMessageID());
//    					producer.send(replyMessage);
//    					
//    				} catch (JMSException e) {
//    					e.printStackTrace();
//    				}
//    			}
//    		});
    		
    		// 這個接收者用來接收回復的消息
    		MessageConsumer comsumer_RecvProcess = session.createConsumer(reply_dest,
    				"JMSCorrelationID='" + msg.getJMSMessageID() + "'");
    		comsumer_RecvProcess.receive(timeout);
    		comsumer_RecvProcess.setMessageListener(new MessageListener() {
    			public void onMessage(Message m) {
    				try {
    					System.out.println("Received message: \n" + ((TextMessage) m).getText());
    				} catch (JMSException e) {
    					e.printStackTrace();
    				}
    			}
    		});
    		
		    //close connection&session
		    connection.close();
		} catch (JMSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
