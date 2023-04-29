#pragma once

#include "TextContainer.h"

// an XML node is an object with an object of attributes, an array of children,
// a name, and a body
MAKE_PTR_TO(XmlNode) {
    private:
        Text name;
        Text content;
        TextMap<Text> attributes;
        vector<PtrToXmlNode> children;

        // depth first recursive traversal
        void internalFind (const Text& findName, vector<PtrToXmlNode>& result) const {
            if (name == findName) {
                result.push_back (this);
            }

            for (vector<PtrToXmlNode>::const_iterator it = children.begin (); it != children.end (); ++it) {
                (*it)->internalFind (findName, result);
            }
        }

        // depth first recursive traversal
        void internalFind (const Text& attributeName, const Text& attributeValue, vector<PtrToXmlNode>& result) const {
            const Text* attribute = attributes.get(attributeName);
            if (attribute && ((*attribute) == attributeValue)) {
                result.push_back (this);
            }
            for (vector<PtrToXmlNode>::const_iterator it = children.begin (); it != children.end (); ++it) {
                (*it)->internalFind (attributeName, attributeValue, result);
            }
        }

    public:
        XmlNode (const Text& _name) : name (_name) {}
        XmlNode (const Text& _name, const Text& _content) : name (_name), content (_content) {}
        virtual ~XmlNode () {}

        Text& getName () { return name; }
        const Text& getName () const { return name; }
        PtrToXmlNode setName (const Text& _name) {
            name = _name;
            return this;
        }
        Text& getContent () { return content; }
        const Text& getContent () const { return content; }
        PtrToXmlNode setContent (const Text& _content) {
            content = _content;
            return this;
        }

        bool hasAttribute (const Text& attributeName) {
            return attributes.contains(attributeName);
        }
        Text getAttribute (const Text& attributeName) const {
            const Text* attributeValue =  attributes.get(attributeName);
            return (attributeValue) ? *attributeValue : Text ();
        }
        PtrToXmlNode setAttribute (const Text& attributeName, const Text& attributeValue) {
            attributes.set(attributeName, attributeValue);
            return this;
        }

        TextMap<Text>& getAttributes () { return attributes; }
        const TextMap<Text>& getAttributes () const { return attributes; }

        PtrToXmlNode addChild (const PtrToXmlNode& child) {
            children.push_back(child);
            return this;
        }
        vector<PtrToXmlNode> getChild (const Text& childName) const {
            vector<PtrToXmlNode> result;
            for (vector<PtrToXmlNode>::const_iterator it = children.begin (); it != children.end (); ++it) {
                if ((*it)->name == childName) {
                    result.push_back (*it);
                }
            }
            return result;
        }

        vector<PtrToXmlNode>& getChildren () { return children; }
        const vector<PtrToXmlNode>& getChildren () const { return children; }

        vector<PtrToXmlNode> find (const Text& findName) const {
            vector<PtrToXmlNode> result;
            internalFind (findName, result);
            return result;
        }

        vector<PtrToXmlNode> find (const Text& attributeName, const Text& attributeValue) const {
            vector<PtrToXmlNode> result;
            internalFind (attributeName, attributeValue, result);
            return result;
        }

        Text toXml () const {
            Text out;

            // open the tag
            out << "<" << name;

            // write the attributes
            for (TextMap<Text>::const_iterator it = attributes.begin (); it != attributes.end (); ++it) {
                out << " " << it->first << "=\"" << it->second << "\"";
            }

            // check to see if there is sub-content
            bool hasChildren = (children.size () > 0);
            bool hasContent = (content.length() > 0);
            if (hasChildren or hasContent) {
                // there is, so we have to close the opening tag and write the sub-content
                out << ">";

                // write the children
                if (hasChildren) {
                    for (vector<PtrToXmlNode>::const_iterator it = children.begin (); it != children.end (); ++it) {
                        out << (*it)->toXml ();
                    }
                }

                // write the content, be careful about extra content accidentally added here (like END_LINE)
                if (hasContent) {
                    out << content;
                }

                // add the closing tag
                out << "</" << name << ">";
            } else {
                out << "/>";
            }

            return out;
        }
};

