/*
 * -----------------------------------------------------------------------\
 * SilverWare
 *  
 * Copyright (C) 2010 - 2013 the original author or authors.
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * -----------------------------------------------------------------------/
 */
package org.silverware.microservices.silver.services;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.silverware.microservices.Context;
import org.silverware.microservices.MicroserviceMetaData;
import org.silverware.microservices.annotations.InvocationPolicy;
import org.silverware.microservices.silver.services.lookup.LocalLookupStrategy;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.util.Set;

/**
 * @author Martin Večeřa <marvenec@gmail.com>
 */
public class LookupStrategyFactory {

   /**
    * Logger.
    */
   private static Logger log = LogManager.getLogger(LookupStrategyFactory.class);

   public static LookupStrategy getStrategy(final Context context, final MicroserviceMetaData metaData, final Set<Annotation> options) {
      LookupStrategy strategy = null;

      for (Annotation option: options) {
         if (option.annotationType().isAssignableFrom(InvocationPolicy.class)) {
            InvocationPolicy policy = (InvocationPolicy) option;
            Class<LookupStrategy> clazz = policy.lookupStrategy();

            try {
               Constructor c = clazz.getConstructor();
               strategy = (LookupStrategy) c.newInstance();
               strategy.initialize(context, metaData, options);
               break;
            } catch (Exception e) {
               log.warn(String.format("Could not instantiate lookup strategy class %s:", clazz.getName()), e);
            }
         }
      }

      if (strategy == null) {
         strategy = new LocalLookupStrategy();
         strategy.initialize(context, metaData, options);
      }

      return strategy;
   }
}
