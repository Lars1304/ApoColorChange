package com.assentis.apobank.apocolorchange.staging;

import com.sun.org.apache.xpath.internal.jaxp.XPathFactoryImpl;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.namespace.NamespaceContext;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Staging {

    private Document staging = null;

    private Schemas schemas;
    private Folders folders;
    private RepoElements elements;

    private XPath xpath;

    public Staging(Document doc) throws XPathExpressionException {
        staging = doc;
        schemas = new Schemas();
        folders = new Folders();
        elements = new RepoElements();
        load(doc);
    }

    private void load(Document doc) throws XPathExpressionException {
        xpath = getNamespaceAwareXpath();

        getSchemas(doc);
        getFolders(doc);
        getElements(doc);
    }

    public List<RepoElement> getElementByType(String elementType) {
        if (elementType == null) {
            return null;
        }
        ArrayList<RepoElement> elementList = new ArrayList<>();

        for (RepoElement element : elements.getAll()) {
            if (elementType.equals(element.getElementType())) {
                elementList.add(element);
            }
        }
        return elementList;
    }

    public Document purgeStaging() throws XPathExpressionException {
        Document newStaging = (Document) staging.cloneNode(true);

        purge(newStaging,1); //Schemas
        purge(newStaging,2); //Folders
        purge(newStaging,3); //Elements


        return newStaging;
    }

    private void purge(Document doc, int type) throws XPathExpressionException {   // type: 1=schema, 2=folder, 3=elements
        NodeList nodes = null;
        switch (type) {
            case 1  :   nodes = (NodeList) xpath.evaluate("/StagingFile/Schema", doc, XPathConstants.NODESET);
                        break;
            case 2  :   nodes = (NodeList) xpath.evaluate("/StagingFile/Folder", doc, XPathConstants.NODESET);
                break;
            case 3  :   nodes = (NodeList) xpath.evaluate("/StagingFile/Element", doc, XPathConstants.NODESET);
                break;
        }

        for (int i = 0; i < nodes.getLength(); i++) {
            Element node = (Element) nodes.item(i);
            String id = null;

            NodeList children = node.getChildNodes();
            for (int j = 0; j < children.getLength(); j++) {
                Node child = children.item(j);
                if ("DbKey".equals(child.getNodeName())) {
                    id = child.getTextContent();
                }
            }
            switch (type) {
                case 1:
                    if (!schemas.get(id).isChanged()) {
                        node.getParentNode().removeChild(node);
                    }
                    break;
                case 2:
                    if (!folders.get(id).isChanged()) {
                        node.getParentNode().removeChild(node);
                    }
                    break;
                case 3:
                    if (!elements.get(id).isChanged()) {
                        node.getParentNode().removeChild(node);
                    }
                    break;
            }
        }
    }


    public String getElementPath(RepoElement element) {
        Folder folder = folders.get(element.getFolderId());
        Schema schema = schemas.get(folder.getSchemaId());

        return "/" + schema.getName() + folders.getPath(folder.getDbKey()) + "/" + element.getName();
    }

    private void getSchemas(Document doc) throws XPathExpressionException {
        NodeList nodes = (NodeList) xpath.evaluate("/StagingFile/Schema", doc, XPathConstants.NODESET);

        for (int i = 0; i < nodes.getLength(); i++) {
            Element schemaNode = (Element) nodes.item(i);
            Schema schema = new Schema();

            NodeList schemaChildren = schemaNode.getChildNodes();
            for (int j=0; j < schemaChildren.getLength(); j++) {
                Node schemaChild = schemaChildren.item(j);
                if ("DbKey".equals(schemaChild.getNodeName())) {
                    schema.setDbKey(schemaChild.getTextContent());
                } else if ("Name".equals(schemaChild.getNodeName())) {
                    schema.setName(schemaChild.getTextContent());
                }
            }
            schemas.add(schema);
        }
    }

    private void getFolders(Document doc) throws XPathExpressionException {
        NodeList nodes = (NodeList) xpath.evaluate("/StagingFile/Folder", doc, XPathConstants.NODESET);

        for (int i = 0; i < nodes.getLength(); i++) {
            Element folderNode = (Element) nodes.item(i);
            Folder folder = new Folder();

            NodeList folderChildren = folderNode.getChildNodes();
            for (int j=0; j < folderChildren.getLength(); j++) {
                Node folderChild = folderChildren.item(j);
                if ("DbKey".equals(folderChild.getNodeName())) {
                    folder.setDbKey(folderChild.getTextContent());
                } else if ("Name".equals(folderChild.getNodeName())) {
                    folder.setName(folderChild.getTextContent());
                } else if ("ParentId".equals(folderChild.getNodeName())) {
                    folder.setParentId(folderChild.getTextContent());
                } else if ("SchemaId".equals(folderChild.getNodeName())) {
                    folder.setSchemaId(folderChild.getTextContent());
                }
            }
            folders.add(folder);
        }
    }

    private void getElements(Document doc) throws XPathExpressionException {
        NodeList nodes = (NodeList) xpath.evaluate("/StagingFile/Element", doc, XPathConstants.NODESET);

        for (int i = 0; i < nodes.getLength(); i++) {
            Element elementsNode = (Element) nodes.item(i);
            RepoElement element = new RepoElement();

            NodeList elementChildren = elementsNode.getChildNodes();
            for (int j=0; j < elementChildren.getLength(); j++) {
                Node elementChild = elementChildren.item(j);
                if ("DbKey".equals(elementChild.getNodeName())) {
                    element.setDbKey(elementChild.getTextContent());
                } else if ("Name".equals(elementChild.getNodeName())) {
                    element.setName(elementChild.getTextContent());
                } else if ("FolderId".equals(elementChild.getNodeName())) {
                    element.setFolderId(elementChild.getTextContent());
                } else if ("ElementId".equals(elementChild.getNodeName())) {
                    element.setElementId(elementChild.getTextContent());
                } else if ("ElementType".equals(elementChild.getNodeName())) {
                    element.setElementType(elementChild.getTextContent());
                }
            }
            element.setElementPath(getElementPath(element));
            elements.add(element);
        }
    }

    private XPath getNamespaceAwareXpath() {
        XPath xPath = new XPathFactoryImpl().newXPath();
        xPath.setNamespaceContext(new NamespaceContext() {
            @Override
            public String getNamespaceURI(String prefix) {
                switch (prefix) {
                    case "a":
                        return "http://www.assentis.com/schema/afo";
                    case "fo":
                        return "http://www.w3.org/1999/XSL/Format";
                    case "write":
                        return "http://www.assentis.com/write/html";
                    default:
                        throw new IllegalArgumentException("No prefix provided!");
                }
            }

            @Override
            public String getPrefix(String namespaceURI) {
                return null;
            }

            @Override
            public Iterator<?> getPrefixes(String namespaceURI) {
                return null;
            }
        });
        return xPath;
    }

}
