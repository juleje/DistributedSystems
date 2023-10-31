package be.kuleuven.distributedsystems.cloud.auth;

import be.kuleuven.distributedsystems.cloud.entities.User;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.security.PublicKey;
import java.util.Base64;
import java.util.Collection;

@Component
public class SecurityFilter extends OncePerRequestFilter {



    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
/*
        // idToken comes from the client app (shown above)
        FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(idToken);
        String uid = decodedToken.getUid();

 */
        // TODO: (level 1) decode Identity Token and assign correct email and role
        String authorizationHeaderValue = request.getHeader("Authorization");
        if (authorizationHeaderValue != null && authorizationHeaderValue.startsWith("Bearer")) {
            System.out.println(authorizationHeaderValue);

            try{

                String token = authorizationHeaderValue.replaceAll("Bearer ","");
                Base64.Decoder decoder = Base64.getUrlDecoder();
                String[] chunks = token.split("\\.");
                String header = new String(decoder.decode(chunks[0]));
                String payload = new String(decoder.decode(chunks[1]));
                System.out.println( payload);
                /*
                var kid = JWT.decode(authorizationHeaderValue).getKeyId();
                var pubKey = "PublicKey";
                Algorithm algorithm = Algorithm.RSA256(pubKey);
                DecodedJWT jwt = JWT.require(algorithm)
                        .withIssuer("https://securetoken.google.com/demo-distributed-systems-kul")
                        .build()
                        .verify(authorizationHeaderValue);
                var role = jwt.getClaim("role");
                System.out.println(role);
                System.out.println(jwt.getClaim("email"));
                 */

            }catch(JWTVerificationException ex){
                //unautherizd
            }
        }else{
            //unautherized
        }



        // TODO: (level 2) verify Identity Token




        var user = new User("test@example.com", new String[]{});
        SecurityContext context = SecurityContextHolder.getContext();
        context.setAuthentication(new FirebaseAuthentication(user));

        filterChain.doFilter(request, response);
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

