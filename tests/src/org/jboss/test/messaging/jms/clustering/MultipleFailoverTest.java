/*
* JBoss, Home of Professional Open Source
* Copyright 2005, JBoss Inc., and individual contributors as indicated
* by the @authors tag. See the copyright.txt in the distribution for a
* full listing of individual contributors.
*
* This is free software; you can redistribute it and/or modify it
* under the terms of the GNU Lesser General Public License as
* published by the Free Software Foundation; either version 2.1 of
* the License, or (at your option) any later version.
*
* This software is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
* Lesser General Public License for more details.
*
* You should have received a copy of the GNU Lesser General Public
* License along with this software; if not, write to the Free
* Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
* 02110-1301 USA, or see the FSF site: http://www.fsf.org.
*/
package org.jboss.test.messaging.jms.clustering;

import org.jboss.test.messaging.jms.clustering.base.ClusteringTestBase;
import org.jboss.test.messaging.tools.ServerManagement;

import javax.jms.Connection;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.MessageProducer;
import javax.jms.MessageConsumer;

/**
 * A test where we kill multiple nodes and make sure the failover works correctly in these condtions
 * too.
 *
 * @author <a href="mailto:ovidiu@jboss.org">Ovidiu Feodorov</a>
 * @version <tt>$Revision$</tt>
 *
 * $Id$
 */
public class MultipleFailoverTest extends ClusteringTestBase
{
   // Constants ------------------------------------------------------------------------------------

   // Static ---------------------------------------------------------------------------------------

   // Attributes -----------------------------------------------------------------------------------

   // Constructors ---------------------------------------------------------------------------------

   public MultipleFailoverTest(String name)
   {
      super(name);
   }

   // Public ---------------------------------------------------------------------------------------

   public void testAllKindsOfServerFailures() throws Exception
   {
      Connection conn = null;
      TextMessage m = null;
      MessageProducer prod = null;
      MessageConsumer cons = null;

      try
      {
         // we start with a cluster of two (server 0 and server 1)

         conn = cf.createConnection();
         conn.start();

         // send/receive message
         Session s = conn.createSession(false, Session.AUTO_ACKNOWLEDGE);
         prod = s.createProducer(queue[0]);
         cons = s.createConsumer(queue[0]);
         prod.send(s.createTextMessage("step1"));
         m = (TextMessage)cons.receive();
         assertNotNull(m);
         assertEquals("step1", m.getText());

         log.info("killing node 0 ....");

         ServerManagement.kill(0);

         log.info("########");
         log.info("######## KILLED NODE 0");
         log.info("########");

         // send/receive message
         prod.send(s.createTextMessage("step2"));
         m = (TextMessage)cons.receive();
         assertNotNull(m);
         assertEquals("step2", m.getText());

         log.info("########");
         log.info("######## STARTING NODE 2");
         log.info("########");

         ServerManagement.start(2, "all");
         ServerManagement.deployQueue("testDistributedQueue", 2);

         // send/receive message
         prod.send(s.createTextMessage("step3"));
         m = (TextMessage)cons.receive();
         assertNotNull(m);
         assertEquals("step3", m.getText());

         log.info("killing node 1 ....");

         ServerManagement.kill(1);

         log.info("########");
         log.info("######## KILLED NODE 1");
         log.info("########");

         // send/receive message
         prod.send(s.createTextMessage("step4"));
         m = (TextMessage)cons.receive();
         assertNotNull(m);
         assertEquals("step4", m.getText());

         log.info("########");
         log.info("######## STARTING NODE 3");
         log.info("########");

         ServerManagement.start(3, "all");
         ServerManagement.deployQueue("testDistributedQueue", 3);

         // send/receive message
         prod.send(s.createTextMessage("step5"));
         m = (TextMessage)cons.receive();
         assertNotNull(m);
         assertEquals("step5", m.getText());

         log.info("killing node 2 ....");

         ServerManagement.kill(2);

         log.info("########");
         log.info("######## KILLED NODE 2");
         log.info("########");

         // send/receive message
         prod.send(s.createTextMessage("step6"));
         m = (TextMessage)cons.receive();
         assertNotNull(m);
         assertEquals("step6", m.getText());

         log.info("########");
         log.info("######## STARTING NODE 0");
         log.info("########");

         ServerManagement.start(0, "all");
         ServerManagement.deployQueue("testDistributedQueue", 0);

         // send/receive message
         prod.send(s.createTextMessage("step7"));
         m = (TextMessage)cons.receive();
         assertNotNull(m);
         assertEquals("step7", m.getText());

         log.info("killing node 3 ....");

         ServerManagement.kill(3);

         log.info("########");
         log.info("######## KILLED NODE 3");
         log.info("########");

         // send/receive message
         prod.send(s.createTextMessage("step8"));
         m = (TextMessage)cons.receive();
         assertNotNull(m);
         assertEquals("step8", m.getText());

      }
      finally
      {
         if (conn != null)
         {
            conn.close();
         }
      }
   }

   // Package protected ----------------------------------------------------------------------------

   // Protected ------------------------------------------------------------------------------------

   protected void setUp() throws Exception
   {
      nodeCount = 2;

      super.setUp();

      log.debug("setup done");
   }

   protected void tearDown() throws Exception
   {
      super.tearDown();
   }

   // Private --------------------------------------------------------------------------------------

   // Inner classes --------------------------------------------------------------------------------

}
