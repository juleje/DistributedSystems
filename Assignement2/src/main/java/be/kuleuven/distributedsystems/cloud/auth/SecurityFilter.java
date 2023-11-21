package be.kuleuven.distributedsystems.cloud.auth;

import be.kuleuven.distributedsystems.cloud.entities.User;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.net.URI;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;
import java.util.*;

@Component
public class SecurityFilter extends OncePerRequestFilter {

    private static final String pubKeyUrl = "https://www.googleapis.com/robot/v1/metadata/x509/securetoken@system.gserviceaccount.com";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        // (level 1) decode Identity Token and assign correct email and role
        String authorizationHeaderValue = request.getHeader("Authorization");
        if (authorizationHeaderValue != null && authorizationHeaderValue.startsWith("Bearer")) {

            String token = authorizationHeaderValue.replaceAll("Bearer ","");
            Base64.Decoder decoder = Base64.getUrlDecoder();
            String[] chunks = token.split("\\.");
            String payload = new String(decoder.decode(chunks[1]));

            User user = null;
            try {
                JSONObject json = new JSONObject(payload);
                String[] roles = new String[1];
                try {
                    roles[0] = json.getString("roles");
                } catch (JSONException e) {
                    System.out.println(e.getMessage());
                }
                user = new User(json.getString("email"),  roles);
            } catch (JSONException e) {
                System.out.println(e.getMessage());
            }


            SecurityContext context = SecurityContextHolder.getContext();
            context.setAuthentication(new FirebaseAuthentication(user));

            List<String> restrictedEndpoints = new ArrayList<>();
            restrictedEndpoints.add("/api/getAllBookings");
            restrictedEndpoints.add("/api/getBestCustomers");

            assert user != null;
            if(!user.isManager()&&restrictedEndpoints.contains(request.getRequestURI())){

                System.out.println("User " +user.getEmail()+ " is unauhterized to acces endpoint: "+request.getRequestURI());
                response.sendError(401);
            }


            // get public keys
            JsonObject publicKeys = getPublicKeysJson();

            for (Map.Entry<String, JsonElement> entry: publicKeys.entrySet()) {
                System.out.println(entry.getKey());
                System.out.println(entry.getValue().toString());
                //Jwts.parser().setSigningKey(publicKey).parse(token.getTokenId());
            }
/*

            var kid = JWT.decode(token).getKeyId();
            Algorithm algorithm = Algorithm.RSA256(publicKey,null);
            DecodedJWT jwt = JWT.require(algorithm)
                    .withIssuer("https://securetoken.google.com/demo-distributed-systems-kul")
                    .build()
                    .verify(token);
            System.out.println(jwt.getClaim("email"));

 */

        }else {
            System.out.println("User is not authenticated");
        }


        filterChain.doFilter(request, response);

    }

    private JsonObject getPublicKeysJson() throws IOException {
        // get public keys
        URI uri = URI.create(pubKeyUrl);
        GenericUrl url = new GenericUrl(uri);
        HttpTransport http = new NetHttpTransport();
        HttpResponse response = http.createRequestFactory().buildGetRequest(url).execute();

        // store json from request
        String json = response.parseAsString();
        // disconnect
        response.disconnect();

        // parse json to object
        JsonObject jsonObject = new JsonParser().parse(json).getAsJsonObject();

        return jsonObject;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI().substring(request.getContextPath().length());
        return !path.startsWith("/api");
    }

    public static User getUser() {
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    private static class FirebaseAuthentication implements Authentication {
        private final User user;

        FirebaseAuthentication(User user) {
            this.user = user;
        }

        @Override
        public Collection<? extends GrantedAuthority> getAuthorities() {
            return null;
        }

        @Override
        public Object getCredentials() {
            return null;
        }

        @Override
        public Object getDetails() {
            return null;
        }

        @Override
        public User getPrincipal() {
            return this.user;
        }

        @Override
        public boolean isAuthenticated() {
            return true;
        }

        @Override
        public void setAuthenticated(boolean b) throws IllegalArgumentException {

        }

        @Override
        public String getName() {
            return null;
        }
    }
}

