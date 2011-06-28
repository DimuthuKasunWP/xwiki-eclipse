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
 */
package org.xwiki.eclipse.storage;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.xwiki.eclipse.model.ModelObject;
import org.xwiki.eclipse.model.XWikiEclipseAttachment;
import org.xwiki.eclipse.model.XWikiEclipseObjectSummary;
import org.xwiki.eclipse.model.XWikiEclipsePageSummary;
import org.xwiki.eclipse.model.XWikiEclipseServerInfo;
import org.xwiki.eclipse.model.XWikiEclipseSpaceSummary;
import org.xwiki.eclipse.model.XWikiEclipseWikiSummary;
import org.xwiki.eclipse.rest.Relations;
import org.xwiki.eclipse.rest.RestRemoteXWikiDataStorage;
import org.xwiki.rest.model.jaxb.Attachment;
import org.xwiki.rest.model.jaxb.Link;
import org.xwiki.rest.model.jaxb.ObjectSummary;
import org.xwiki.rest.model.jaxb.PageSummary;
import org.xwiki.rest.model.jaxb.Space;
import org.xwiki.rest.model.jaxb.Wiki;
import org.xwiki.rest.model.jaxb.Xwiki;

/**
 * @version $Id$
 */
public class RestRemoteXWikiDataStorageAdapter implements IRemoteXWikiDataStorage
{
    RestRemoteXWikiDataStorage restRemoteStorage;

    private DataManager dataManager;

    private String password;

    private String username;

    private String endpoint;

    /**
     * @param dataManager
     * @param endpoint
     * @param userName
     * @param password
     */
    public RestRemoteXWikiDataStorageAdapter(DataManager dataManager, String endpoint, String userName, String password)
    {
        this.dataManager = dataManager;
        this.restRemoteStorage = new RestRemoteXWikiDataStorage(endpoint, userName, password);
        this.endpoint = endpoint;
        this.username = userName;
        this.password = password;
    }

    public List<XWikiEclipseWikiSummary> getWikis() throws XWikiEclipseStorageException
    {
        try {
            List<XWikiEclipseWikiSummary> result = new ArrayList<XWikiEclipseWikiSummary>();

            List<Wiki> wikis = restRemoteStorage.getWikis(username, password);
            for (Wiki wiki : wikis) {
                XWikiEclipseWikiSummary wikiSummary = new XWikiEclipseWikiSummary(dataManager);
                wikiSummary.setWikiId(wiki.getId());
                wikiSummary.setName(wiki.getName());
                List<Link> links = wiki.getLinks();
                for (Link link : links) {
                    if (link.getRel().equals(Relations.SPACES)) {
                        wikiSummary.setSpacesUrl(link.getHref());
                        break;
                    }
                }

                result.add(wikiSummary);
            }

            return result;
        } catch (Exception e) {
            e.printStackTrace();
            throw new XWikiEclipseStorageException(e);
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.eclipse.storage.IRemoteXWikiDataStorage#getSpaces()
     */
    @Override
    public List<XWikiEclipseSpaceSummary> getSpaces(XWikiEclipseWikiSummary wiki)
    {
        List<XWikiEclipseSpaceSummary> result = new ArrayList<XWikiEclipseSpaceSummary>();

        List<Space> spaces = this.restRemoteStorage.getSpaces(wiki.getSpacesUrl(), username, password);
        if (spaces != null) {
            for (Space space : spaces) {
                XWikiEclipseSpaceSummary summary = new XWikiEclipseSpaceSummary(dataManager);
                summary.setKey(space.getId());
                summary.setName(space.getName());
                summary.setUrl(space.getXwikiAbsoluteUrl());
                summary.setWiki(space.getWiki());
                List<Link> links = space.getLinks();
                for (Link link : links) {
                    if (link.getRel().equals(Relations.PAGES)) {
                        summary.setPagesUrl(link.getHref());
                        break;
                    }
                }

                result.add(summary);
            }
        }

        return result;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.eclipse.storage.IRemoteXWikiDataStorage#dispose()
     */
    @Override
    public void dispose()
    {
        // TODO Auto-generated method stub

    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.eclipse.storage.IRemoteXWikiDataStorage#getServerInfo()
     */
    @Override
    public XWikiEclipseServerInfo getServerInfo()
    {
        XWikiEclipseServerInfo serverInfo = new XWikiEclipseServerInfo();

        Xwiki xwiki = this.restRemoteStorage.getServerInfo();

        String versionStr = xwiki.getVersion(); // e.g., 3.1-rc-1
        StringTokenizer tokenizer = new StringTokenizer(versionStr, "-");

        String v = tokenizer.nextToken();

        tokenizer = new StringTokenizer(v, ".");
        serverInfo.setMajorVersion(Integer.parseInt(tokenizer.nextToken()));
        serverInfo.setMinorVersion(Integer.parseInt(tokenizer.nextToken()));

        serverInfo.setBaseUrl(endpoint);

        return serverInfo;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.eclipse.storage.IRemoteXWikiDataStorage#getRootResources()
     */
    @Override
    public List<ModelObject> getRootResources()
    {
        List<ModelObject> result = new ArrayList<ModelObject>();

        List<XWikiEclipseWikiSummary> wikis;
        try {
            wikis = getWikis();
            for (XWikiEclipseWikiSummary wikiSummary : wikis) {
                result.add(wikiSummary);
            }
            return result;
        } catch (XWikiEclipseStorageException e) {
            e.printStackTrace();
        }

        return null;

    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.eclipse.storage.IRemoteXWikiDataStorage#getPages(org.xwiki.eclipse.model.XWikiEclipseSpaceSummary)
     */
    @Override
    public List<XWikiEclipsePageSummary> getPages(XWikiEclipseSpaceSummary spaceSummary)
    {
        List<XWikiEclipsePageSummary> result = new ArrayList<XWikiEclipsePageSummary>();

        List<PageSummary> pages = this.restRemoteStorage.getPages(spaceSummary.getPagesUrl(), username, password);
        for (PageSummary pageSummary : pages) {
            XWikiEclipsePageSummary page = new XWikiEclipsePageSummary(dataManager);
            page.setId(pageSummary.getId());
            page.setParentId(pageSummary.getParentId());
            page.setSpace(pageSummary.getSpace());
            page.setTitle(pageSummary.getTitle());
            page.setUrl(pageSummary.getXwikiAbsoluteUrl());
            page.setWiki(pageSummary.getWiki());

            List<Link> links = pageSummary.getLinks();
            for (Link link : links) {
                if (link.getRel().equals(Relations.OBJECTS)) {
                    page.setObjectsUrl(link.getHref());
                }

                if (link.getRel().equals(Relations.ATTACHMENTS)) {
                    page.setAttachmentsUrl(link.getHref());
                }
            }

            result.add(page);
        }

        return result;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.eclipse.storage.IRemoteXWikiDataStorage#getObjects(org.xwiki.eclipse.model.XWikiEclipsePageSummary)
     */
    @Override
    public List<XWikiEclipseObjectSummary> getObjects(XWikiEclipsePageSummary pageSummary)
    {
        List<XWikiEclipseObjectSummary> result = new ArrayList<XWikiEclipseObjectSummary>();

        List<ObjectSummary> objects =
            this.restRemoteStorage.getObjects(pageSummary.getObjectsUrl(), username, password);

        if (objects != null) {
            for (ObjectSummary objectSummary : objects) {
                XWikiEclipseObjectSummary o = new XWikiEclipseObjectSummary(dataManager);
                o.setClassName(objectSummary.getClassName());
                o.setId(objectSummary.getId());
                o.setPageId(objectSummary.getPageId());
                o.setPageName(objectSummary.getPageName());
                o.setSpace(pageSummary.getSpace());
                o.setWiki(pageSummary.getWiki());

                /* set up pretty name = className + [number] */
                String className = objectSummary.getClassName();
                int number = objectSummary.getNumber();
                String prettyName = className + "[" + number + "]";
                o.setPrettyName(prettyName);

                result.add(o);
            }
        }

        return result;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.eclipse.storage.IRemoteXWikiDataStorage#getAttachments(org.xwiki.eclipse.model.XWikiEclipsePageSummary)
     */
    @Override
    public List<XWikiEclipseAttachment> getAttachments(XWikiEclipsePageSummary pageSummary)
    {
        List<XWikiEclipseAttachment> result = new ArrayList<XWikiEclipseAttachment>();

        List<Attachment> attachments =
            this.restRemoteStorage.getAttachments(pageSummary.getAttachmentsUrl(), username, password);
        if (attachments != null) {
            for (Attachment attachment : attachments) {
                XWikiEclipseAttachment a = new XWikiEclipseAttachment(dataManager);
                a.setAbsoluteUrl(attachment.getXwikiAbsoluteUrl());
                a.setAuthor(attachment.getAuthor());
                a.setDate(attachment.getDate());
                a.setId(attachment.getId());
                a.setMimeType(attachment.getMimeType());
                a.setName(attachment.getName());
                a.setPageId(attachment.getPageId());
                a.setPageVersion(attachment.getPageVersion());
                a.setVersion(attachment.getVersion());

                List<Link> links = attachment.getLinks();
                for (Link link : links) {
                    if (link.getRel().equals(Relations.ATTACHMENT_DATA)) {
                        a.setAttachmentUrl(link.getHref());
                    }

                    if (link.getRel().equals(Relations.PAGE)) {
                        a.setPageUrl(link.getHref());
                    }
                }

                result.add(a);
            }
        }

        return result;
    }
}
