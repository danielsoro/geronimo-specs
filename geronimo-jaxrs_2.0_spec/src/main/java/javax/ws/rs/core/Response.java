/*
 * #%L
 * Apache Geronimo JAX-RS Spec 2.0
 * %%
 * Copyright (C) 2003 - 2014 The Apache Software Foundation
 * %%
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
 * #L%
 */

package javax.ws.rs.core;

import javax.ws.rs.ext.RuntimeDelegate;
import java.lang.annotation.Annotation;
import java.net.URI;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;


public abstract class Response {


    protected Response() {
    }


    public abstract int getStatus();


    public abstract StatusType getStatusInfo();


    public abstract Object getEntity();


    public abstract <T> T readEntity(Class<T> entityType);


    public abstract <T> T readEntity(GenericType<T> entityType);


    public abstract <T> T readEntity(Class<T> entityType, Annotation[] annotations);


    public abstract <T> T readEntity(GenericType<T> entityType, Annotation[] annotations);


    public abstract boolean hasEntity();


    public abstract boolean bufferEntity();


    public abstract void close();


    public abstract MediaType getMediaType();


    public abstract Locale getLanguage();


    public abstract int getLength();


    public abstract Set<String> getAllowedMethods();


    public abstract Map<String, NewCookie> getCookies();


    public abstract EntityTag getEntityTag();


    public abstract Date getDate();


    public abstract Date getLastModified();


    public abstract URI getLocation();


    public abstract Set<Link> getLinks();


    public abstract boolean hasLink(String relation);


    public abstract Link getLink(String relation);


    public abstract Link.Builder getLinkBuilder(String relation);


    public abstract MultivaluedMap<String, Object> getMetadata();


    public MultivaluedMap<String, Object> getHeaders() {
        return getMetadata();
    }


    public abstract MultivaluedMap<String, String> getStringHeaders();


    public abstract String getHeaderString(String name);


    public static ResponseBuilder fromResponse(Response response) {
        ResponseBuilder b = status(response.getStatus());
        if (response.hasEntity()) {
            b.entity(response.getEntity());
        }
        for (String headerName : response.getHeaders().keySet()) {
            List<Object> headerValues = response.getHeaders().get(headerName);
            for (Object headerValue : headerValues) {
                b.header(headerName, headerValue);
            }
        }
        return b;
    }


    public static ResponseBuilder status(StatusType status) {
        return ResponseBuilder.newInstance().status(status);
    }


    public static ResponseBuilder status(Status status) {
        return status((StatusType) status);
    }


    public static ResponseBuilder status(int status) {
        return ResponseBuilder.newInstance().status(status);
    }


    public static ResponseBuilder ok() {
        return status(Status.OK);
    }


    public static ResponseBuilder ok(Object entity) {
        ResponseBuilder b = ok();
        b.entity(entity);
        return b;
    }


    public static ResponseBuilder ok(Object entity, MediaType type) {
        return ok().entity(entity).type(type);
    }


    public static ResponseBuilder ok(Object entity, String type) {
        return ok().entity(entity).type(type);
    }


    public static ResponseBuilder ok(Object entity, Variant variant) {
        return ok().entity(entity).variant(variant);
    }


    public static ResponseBuilder serverError() {
        return status(Status.INTERNAL_SERVER_ERROR);
    }


    public static ResponseBuilder created(URI location) {
        return status(Status.CREATED).location(location);
    }


    public static ResponseBuilder accepted() {
        return status(Status.ACCEPTED);
    }


    public static ResponseBuilder accepted(Object entity) {
        return accepted().entity(entity);
    }


    public static ResponseBuilder noContent() {
        return status(Status.NO_CONTENT);
    }


    public static ResponseBuilder notModified() {
        return status(Status.NOT_MODIFIED);
    }


    public static ResponseBuilder notModified(EntityTag tag) {
        return notModified().tag(tag);
    }


    @SuppressWarnings("HtmlTagCanBeJavadocTag")
    public static ResponseBuilder notModified(String tag) {
        return notModified().tag(tag);
    }


    public static ResponseBuilder seeOther(URI location) {
        return status(Status.SEE_OTHER).location(location);
    }


    public static ResponseBuilder temporaryRedirect(URI location) {
        return status(Status.TEMPORARY_REDIRECT).location(location);
    }


    public static ResponseBuilder notAcceptable(List<Variant> variants) {
        return status(Status.NOT_ACCEPTABLE).variants(variants);
    }


    public static abstract class ResponseBuilder {


        protected ResponseBuilder() {
        }


        protected static ResponseBuilder newInstance() {
            return RuntimeDelegate.getInstance().createResponseBuilder();
        }


        public abstract Response build();


        @Override
        @SuppressWarnings("CloneDoesntDeclareCloneNotSupportedException")
        public abstract ResponseBuilder clone();


        public abstract ResponseBuilder status(int status);


        public ResponseBuilder status(StatusType status) {
            if (status == null) {
                throw new IllegalArgumentException();
            }
            return status(status.getStatusCode());
        }


        public ResponseBuilder status(Status status) {
            return status((StatusType) status);
        }


        public abstract ResponseBuilder entity(Object entity);


        public abstract ResponseBuilder entity(Object entity, Annotation[] annotations);


        public abstract ResponseBuilder allow(String... methods);


        public abstract ResponseBuilder allow(Set<String> methods);


        public abstract ResponseBuilder cacheControl(CacheControl cacheControl);


        public abstract ResponseBuilder encoding(String encoding);


        public abstract ResponseBuilder header(String name, Object value);


        public abstract ResponseBuilder replaceAll(MultivaluedMap<String, Object> headers);


        public abstract ResponseBuilder language(String language);


        public abstract ResponseBuilder language(Locale language);


        public abstract ResponseBuilder type(MediaType type);


        public abstract ResponseBuilder type(String type);


        public abstract ResponseBuilder variant(Variant variant);


        public abstract ResponseBuilder contentLocation(URI location);


        public abstract ResponseBuilder cookie(NewCookie... cookies);


        public abstract ResponseBuilder expires(Date expires);


        public abstract ResponseBuilder lastModified(Date lastModified);


        public abstract ResponseBuilder location(URI location);


        public abstract ResponseBuilder tag(EntityTag tag);


        @SuppressWarnings("HtmlTagCanBeJavadocTag")
        public abstract ResponseBuilder tag(String tag);


        public abstract ResponseBuilder variants(Variant... variants);


        public abstract ResponseBuilder variants(List<Variant> variants);


        public abstract ResponseBuilder links(Link... links);


        public abstract ResponseBuilder link(URI uri, String rel);


        public abstract ResponseBuilder link(String uri, String rel);
    }


    public interface StatusType {


        public int getStatusCode();


        public Status.Family getFamily();


        public String getReasonPhrase();
    }


    public enum Status implements StatusType {


        OK(200, "OK"),

        CREATED(201, "Created"),

        ACCEPTED(202, "Accepted"),

        NO_CONTENT(204, "No Content"),

        RESET_CONTENT(205, "Reset Content"),

        PARTIAL_CONTENT(206, "Partial Content"),

        MOVED_PERMANENTLY(301, "Moved Permanently"),

        FOUND(302, "Found"),

        SEE_OTHER(303, "See Other"),

        NOT_MODIFIED(304, "Not Modified"),

        USE_PROXY(305, "Use Proxy"),

        TEMPORARY_REDIRECT(307, "Temporary Redirect"),

        BAD_REQUEST(400, "Bad Request"),

        UNAUTHORIZED(401, "Unauthorized"),

        PAYMENT_REQUIRED(402, "Payment Required"),

        FORBIDDEN(403, "Forbidden"),

        NOT_FOUND(404, "Not Found"),

        METHOD_NOT_ALLOWED(405, "Method Not Allowed"),

        NOT_ACCEPTABLE(406, "Not Acceptable"),

        PROXY_AUTHENTICATION_REQUIRED(407, "Proxy Authentication Required"),

        REQUEST_TIMEOUT(408, "Request Timeout"),

        CONFLICT(409, "Conflict"),

        GONE(410, "Gone"),

        LENGTH_REQUIRED(411, "Length Required"),

        PRECONDITION_FAILED(412, "Precondition Failed"),

        REQUEST_ENTITY_TOO_LARGE(413, "Request Entity Too Large"),

        REQUEST_URI_TOO_LONG(414, "Request-URI Too Long"),

        UNSUPPORTED_MEDIA_TYPE(415, "Unsupported Media Type"),

        REQUESTED_RANGE_NOT_SATISFIABLE(416, "Requested Range Not Satisfiable"),

        EXPECTATION_FAILED(417, "Expectation Failed"),

        INTERNAL_SERVER_ERROR(500, "Internal Server Error"),

        NOT_IMPLEMENTED(501, "Not Implemented"),

        BAD_GATEWAY(502, "Bad Gateway"),

        SERVICE_UNAVAILABLE(503, "Service Unavailable"),

        GATEWAY_TIMEOUT(504, "Gateway Timeout"),

        HTTP_VERSION_NOT_SUPPORTED(505, "HTTP Version Not Supported");
        private final int code;
        private final String reason;
        private final Family family;


        public enum Family {


            INFORMATIONAL,

            SUCCESSFUL,

            REDIRECTION,

            CLIENT_ERROR,

            SERVER_ERROR,

            OTHER;


            public static Family familyOf(final int statusCode) {
                switch (statusCode / 100) {
                    case 1:
                        return Family.INFORMATIONAL;
                    case 2:
                        return Family.SUCCESSFUL;
                    case 3:
                        return Family.REDIRECTION;
                    case 4:
                        return Family.CLIENT_ERROR;
                    case 5:
                        return Family.SERVER_ERROR;
                    default:
                        return Family.OTHER;
                }
            }
        }

        Status(final int statusCode, final String reasonPhrase) {
            this.code = statusCode;
            this.reason = reasonPhrase;
            this.family = Family.familyOf(statusCode);
        }


        @Override
        public Family getFamily() {
            return family;
        }


        @Override
        public int getStatusCode() {
            return code;
        }


        @Override
        public String getReasonPhrase() {
            return toString();
        }


        @Override
        public String toString() {
            return reason;
        }


        public static Status fromStatusCode(final int statusCode) {
            for (Status s : Status.values()) {
                if (s.code == statusCode) {
                    return s;
                }
            }
            return null;
        }
    }
}
