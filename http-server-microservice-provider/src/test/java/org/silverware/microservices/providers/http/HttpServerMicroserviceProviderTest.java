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
package org.silverware.microservices.providers.http;

import org.silverware.microservices.silver.HttpServerSilverService;
import org.silverware.microservices.silver.http.ServletDescriptor;
import org.silverware.microservices.util.BootUtil;
import org.silverware.microservices.util.Utils;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Semaphore;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Martin Večeřa <marvenec@gmail.com>
 */
public class HttpServerMicroserviceProviderTest {

   private static final Semaphore semaphore = new Semaphore(0);

   @Test
   public void httpServerMicroserviceProviderTest() throws Exception {
      final BootUtil bootUtil = new BootUtil();
      final Map<String, Object> platformProperties = bootUtil.getContext().getProperties();
      final Thread platform = bootUtil.getMicroservicePlatform(this.getClass().getPackage().getName());
      platform.start();

      final Properties servletProperties = new Properties();
      servletProperties.setProperty("greeting", "Mr. Wolf");

      int tries = 0;
      HttpServerSilverService http = null;
      while (http == null && tries < 600) {
         http = (HttpServerSilverService) bootUtil.getContext().getProvider(HttpServerSilverService.class);
         Thread.sleep(100);
         tries++;
      }

      Assert.assertNotNull(http, "Unable to obtain Http Server Silverservice.");

      http.deployServlet("test", "", Collections.singletonList(new ServletDescriptor("test", HttpTestServlet.class, "/", servletProperties)));

      Assert.assertEquals(Utils.readFromUrl("http://" + platformProperties.get(HttpServerSilverService.HTTP_SERVER_ADDRESS) + ":" +
            platformProperties.get(HttpServerSilverService.HTTP_SERVER_PORT) + "/test/"), "Hello Mr. Wolf");

      platform.interrupt();
      platform.join();
   }

   public static class HttpTestServlet extends HttpServlet {

      @Override
      protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
         resp.getWriter().append("Hello " + getInitParameter("greeting"));
      }
   }
}