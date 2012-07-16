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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.math.BigDecimal;
import java.util.List;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.test.annotations.derivedidentities.e1.b.specjmapid.ondemand.CustomerDemand;
import org.hibernate.test.annotations.derivedidentities.e1.b.specjmapid.ondemand.CustomerInventoryDemand;
import org.hibernate.test.annotations.derivedidentities.e1.b.specjmapid.ondemand.CustomerInventoryDemandPK;
import org.hibernate.test.annotations.derivedidentities.e1.b.specjmapid.ondemand.ItemDemand;
import org.hibernate.testing.junit4.BaseCoreFunctionalTestCase;
import org.junit.Before;
import org.junit.Test;

public class LazyLoadingTest extends BaseCoreFunctionalTestCase{

   public LazyLoadingTest() {
      System.setProperty( "hibernate.enable_specj_proprietary_syntax", "true" );
      System.setProperty( "hibernate.enable_funcky_on_demand_loading", "true" );
   }

   @Test
   public void testOnDemandLoading()
   {
      Session s;
      Transaction tx;
      s = openSession();
      tx = s.beginTransaction();
      // store entity in datastore 
      CustomerDemand cust = new CustomerDemand("John", "Doe", "123456", "1.0", new BigDecimal(1),new BigDecimal(1), new BigDecimal(5));
      s.persist(cust);
      ItemDemand item = new ItemDemand();
      item.setId("1");
      item.setName("widget");
      s.persist(item);
      s.flush();
      s.clear();
      
      cust.addInventory(item, 1, new BigDecimal(500));
      s.merge(cust);
      
      Integer lazyId = cust.getId();
      tx.commit();
      
      // load the lazy entity, orm configuration loaded during @Before defines loading
      tx = s.beginTransaction();
      CustomerDemand lazyCustomer = (CustomerDemand)s.get(CustomerDemand.class, lazyId);
      assertNotNull(lazyCustomer);
      tx.commit(); // read-only
      s.clear();
      s.close();
      
      // access the association, outside the session that loaded the entity
      List<CustomerInventoryDemand> inventories = cust.getInventories(); // on-demand load
      assertNotNull(inventories);
      assertEquals(1, inventories.size());
      CustomerInventoryDemand inv = inventories.get(0);
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
               CustomerDemand.class,
               CustomerInventoryDemand.class,
               CustomerInventoryDemandPK.class,
               ItemDemand.class

       };
   }
}
