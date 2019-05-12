package track_ninja.playlist_generator.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import track_ninja.playlist_generator.security.models.JwtAuthenticationFilter;

import javax.annotation.Resource;
import javax.sql.DataSource;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig extends WebSecurityConfigurerAdapter implements WebMvcConfigurer {
    @Resource(name = "UserServiceImpl")
    private UserDetailsService userDetailsService;


    @Autowired
    private DataSource securityDataSource;


    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }


    @Autowired
    public void globalUserDetails(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder());
    }

    @Bean
    public JwtAuthenticationFilter authenticationTokenFilterBean() throws Exception {
        return new JwtAuthenticationFilter();
    }

    @Override
    @Bean
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.jdbcAuthentication().dataSource(securityDataSource)
                .usersByUsernameQuery(
                        "select username,password, enabled  from users where username=?")
                .authoritiesByUsernameQuery(
                        "select\n" +
                                "\n" +
                                "a.username,\n" +
                                "b.role_type as role\n" +
                                "\n" +
                                "from users as a\n" +
                                "join authorities as b\n" +
                                "on a.authority_id = b.authority_id where a.username=?");
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {

        http.authorizeRequests()
                .antMatchers("/api/playlist/**").permitAll()
                .antMatchers("/api/login").permitAll()
                .antMatchers("/api/register").permitAll()
                .antMatchers("/api/user/**").hasAnyRole("USER", "ADMIN")
                .antMatchers("/api/admin/**").permitAll()
                .anyRequest().authenticated()
                .and()
                .formLogin()
                .and()
                .logout()
                .and()
                .exceptionHandling().accessDeniedPage("/access-denied").and()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
        http.addFilterBefore(authenticationTokenFilterBean(), UsernamePasswordAuthenticationFilter.class);
        http.cors().and().csrf().disable().httpBasic();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();

        CorsConfiguration config = new CorsConfiguration();
        config.addAllowedHeader("Access-Control-Allow-Origin");
        config.addAllowedHeader("Authorization");
        config.addAllowedHeader("Content-type");
        config.addAllowedOrigin("*");
        config.addAllowedOrigin("https://d2sr9b0y21rlen.cloudfront.net");
        config.addAllowedOrigin("http://d2sr9b0y21rlen.cloudfront.net");
        config.addAllowedOrigin("d2sr9b0y21rlen.cloudfront.net");
        config.addAllowedOrigin("playlistfortravel.eu");
        config.addAllowedOrigin("www.playlistfortravel.eu");
        config.addAllowedOrigin("http://localhost:4200");
        config.addAllowedOrigin("http://playlistgenerator.s3-website.us-east-2.amazonaws.com");
        config.addAllowedMethod("*");

        source.registerCorsConfiguration("/**", config);
        return source;
    }

}
