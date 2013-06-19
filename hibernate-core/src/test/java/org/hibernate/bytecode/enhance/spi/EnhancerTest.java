/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * Copyright (c) 2013, Red Hat Inc. or third-party contributors as
 * indicated by the @author tags or express copyright attribution
 * statements applied by the authors.  All third-party contributions are
 * distributed under license by Red Hat Inc.
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU
 * Lesser General Public License, as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this distribution; if not, write to:
 * Free Software Foundation, Inc.
 * 51 Franklin Street, Fifth Floor
 * Boston, MA  02110-1301  USA
 */

package org.hibernate.bytecode.enhance.spi;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.InputStream;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.NotFoundException;

import org.hibernate.engine.spi.ManagedEntity;
import org.hibernate.tool.enhance.contexts.EnhancedAssociationReference;
import org.junit.Assert;
import org.junit.Test;

/**
 * Test case to check bytecode enhancement operates as expected.
 * 
 * @author Jeremy Whiting
 */
public class EnhancerTest {

	/**
	 * The test will enhance the entity twice and check enhancements are only made once.
	 */
	@Test
	public void testDuplicateEnhancingIgnored() {
		ClassPool pool = ClassPool.getDefault();

		byte[] raw = null;
		try {
			CtClass initialClas = null;
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			DataOutputStream dos;
			try {
				String separator = System.getProperty( "file.separator" );
				char sep = separator.charAt( 0 );
				String path = Boat.class.getName().replace( '.', sep ) + ".class";
				Assert.assertNotNull( separator );
				initialClas = pool.makeClass( new BufferedInputStream( EnhancerTest.class.getClassLoader().getResourceAsStream( path ) ) );
				dos = new DataOutputStream( bos );
				initialClas.toBytecode( dos );
				raw = bos.toByteArray();
				Assert.assertNotNull( raw );
				initialClas.detach();
			}
			finally {
				if ( bos != null )
					bos.close();
				bos = null;
				dos = null;
			}

			if ( null != raw ) {
				Enhancer enhancer = new Enhancer( new EnhancedAssociationReference() );
				Assert.assertNotNull( enhancer );
				InputStream is = null;
				CtClass clas = null;
				try {
					is = new ByteArrayInputStream( raw );
					clas = pool.makeClass( is );
				}
				finally {
					if ( is != null )
						is.close();
					is = null;
				}

				Assert.assertNotNull( clas );
				int count = 0;
				for ( Object c : clas.getAnnotations() ) {
					count += 1;
				}
				Assert.assertEquals( 1, count );

				byte[] enhancedBytecode = enhancer.enhance( clas.getName(), clas.toBytecode() );
				clas.detach();
				clas = null;
				clas = pool.makeClass( new ByteArrayInputStream( enhancedBytecode ) );
				checkForDuplicateInterface( clas );
				// enhance a second time
				enhancedBytecode = enhancer.enhance( clas.getName(), clas.toBytecode() );
				clas.detach();
				clas = null;
				clas = pool.makeClass( new ByteArrayInputStream( enhancedBytecode ) );
				checkForDuplicateInterface( clas );
			}
			else {
				Assert.fail( "Test case not fully executing." );
			}
		}
		catch (Exception e) {
			Assert.fail( "Problem occured." );
		}
	}

	private void checkForDuplicateInterface(CtClass clas) throws NotFoundException {
		Assert.assertNotNull( clas );
		CtClass[] interfaces = clas.getInterfaces();
		Assert.assertNotNull( interfaces );
		boolean found = false;
		boolean twice = false;
		for ( CtClass inter : interfaces ) {
			if ( ManagedEntity.class.getName().equals( inter.getName() ) ) {
				twice = found;
				found = true;
			}
			if ( twice ) {
				break;
			}
		}
		Assert.assertTrue( found );
		// test the second enhancement was a no-op
		Assert.assertFalse( twice );
	}
}
