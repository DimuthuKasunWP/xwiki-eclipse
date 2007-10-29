/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
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
 *
 */
package org.xwiki.xeclipse.model;

import java.util.Collection;

/**
 * The interface for accessing data provided by an XWiki instance. 
 * IXWikiConnection instances can be obtained through {@link XWikiConnectionFactory}
 */
public interface IXWikiConnection
{
    /**
     * @return An unique identifier for the connection.
     */
    public String getId();
    
    /**
     * Connects to the remote XWiki server.
     * 
     * @param password The password to be used in order to access the remote account.
     * @throws XWikiConnectionException
     */
    public void connect(String password) throws XWikiConnectionException;

    /**
     * Disconnects from the remote XWiki server. 
     * @throws XWikiConnectionException 
     */
    public void disconnect() throws XWikiConnectionException;

    /**
     * Dispose the connection manager by releasing all the resources associated with it. This method
     * should be called whenever the connection manager is not used anymore.
     * @throws XWikiConnectionException 
     */
    public void dispose() throws XWikiConnectionException;

    /**
     * @return true is the connection is connected to the remote XWiki server.
     */
    public boolean isConnected();

    /**
     * @return A collection of space descriptors for the available spaces. 
     * @throws XWikiConnectionException
     */
    public Collection<IXWikiSpace> getSpaces() throws XWikiConnectionException;

    /**
     * @param spaceKey The key for the space to be queried.
     * @return A collection of page descriptors for the pages available in the space with the given key.
     * @throws XWikiConnectionException
     */
    public Collection<IXWikiPage> getPages(String spaceKey)
        throws XWikiConnectionException;
    
    /**
     * @param pageId The id of the page to be retrieved.
     * @return The complete page content for the given id.
     * @throws XWikiConnectionException
     */
    public IXWikiPage getPage(String pageId) throws XWikiConnectionException;
    
    public String getUserName();
    public String getServerUrl();
    
    public void addConnectionEstablishedListener(IXWikiConnectionListener listener);
    public void removeConnectionEstablishedListener(IXWikiConnectionListener listener);
}