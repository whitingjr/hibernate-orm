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
package org.hibernate.tool.enhance.contexts;
 
import javassist.CtClass;
import javassist.CtField;

import javax.persistence.Transient;

import org.hibernate.bytecode.enhance.spi.EnhancementContext;

/**
* This object defines a context to enhance Entities for efficient association
* referencing.
* @author Jeremy Whiting
*/
public class EnhancedAssociationReference implements EnhancementContext {
   
   public boolean isEntityClass(CtClass classDescriptor){
      return true;
   }
   
   public ClassLoader getLoadingClassLoader()
   {
      return getClass().getClassLoader();
   }
   public boolean isCompositeClass(CtClass classDescriptor){
      return false;
   }
   public boolean doDirtyCheckingInline(CtClass classDescriptor){
      return false;
   }
   public boolean hasLazyLoadableAttributes(CtClass classDescriptor){
      return true;
   }
   public boolean isLazyLoadable(CtField field){
      return true;
   }
   public CtField[] order(CtField[] fields){
      //TODO: load ordering from configuration.
      return fields;
   }
   public boolean isPersistentField(CtField ctField){
      return ! ctField.hasAnnotation( Transient.class );
   }

}
