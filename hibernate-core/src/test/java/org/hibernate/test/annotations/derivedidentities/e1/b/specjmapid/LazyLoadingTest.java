 /*
  * JBoss, Home of Professional Open Source
  * Copyright 2012, JBoss Inc., and individual contributors as indicated
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

package org.hibernate.test.annotations.derivedidentities.e1.b.specjmapid;

import java.math.BigDecimal;
import java.util.List;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.testing.junit4.BaseCoreFunctionalTestCase;
import org.junit.Assert;
import org.junit.Before;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class LazyLoadingTest extends BaseCoreFunctionalTestCase{

   public LazyLoadingTest() {
      System.setProperty( "hibernate.enable_specj_proprietary_syntax", "true" );
      System.setProperty( "hibernate.enable_funcky_on_demand_loading", "true" );
   }

   public void testOnDemandLoading()
   {
      Session s;
      Transaction tx;
      s = openSession();
      tx = s.beginTransaction();
      // store entity in datastore 
      Customer cust = new Customer("John", "Doe", "123456", "1.0", new BigDecimal(1),new BigDecimal(1), new BigDecimal(5));
      Item item = new Item();
      item.setName("widget");
      cust.addInventory(item, 1, new BigDecimal(500));
      s.persist(cust);
      Integer lazyId = cust.getId();
      tx.commit();
      s.clear();
      
      // load the lazy entity, orm configuration loaded during @Before defines loading
      tx = s.beginTransaction();
      Customer lazyCustomer = (Customer)s.get(Customer.class, lazyId);
      assertNotNull(lazyCustomer);
      tx.commit(); // read-only
      s.clear();
      s.close();
      
      // access the association, outside the session that loaded the entity
      List<CustomerInventory> inventories = cust.getInventories(); // on-demand load
      assertNotNull(inventories);
      assertEquals(1, inventories.size());
      CustomerInventory inv = inventories.get(0);
      assertNotNull(inv);
      assertEquals(inv.getQuantity(), 1); // field access
   }
   
   @Before
   public void setUp()
   {
      configuration().addResource( "org/hibernate/test/annotations/ondemand/orm.xml" );
   }
   
   @Override
   protected Class[] getAnnotatedClasses() {
       return new Class[] {
               Customer.class,
               CustomerInventory.class,
               CustomerInventoryPK.class,
               Item.class

       };
   }
}
