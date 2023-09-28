package latipe.auth.config;

//@Component
//@RequiredArgsConstructor
public class JwtAuthenticationFilter {
//public class JwtAuthenticationFilter extends OncePerRequestFilter {
//    private final JwtTokenService jwtService;
//    private final UserService userDetailsService;
//    @Override
//    protected void doFilterInternal(
//            @NonNull HttpServletRequest request,
//            @NonNull HttpServletResponse response,
//            @NonNull FilterChain filterChain
//    ) throws ServletException, IOException {
//        if (request.getServletPath().contains("/api/auth")) {
//            filterChain.doFilter(request, response);
//            return;
//        }
//        final String authHeader = request.getHeader("Authorization");
//        final String jwt;
//        final String userEmail;
//        if (authHeader == null ||!authHeader.startsWith("Bearer ")) {
//            filterChain.doFilter(request, response);
//            return;
//        }
//        jwt = authHeader.substring(7);
//        try {
//            userEmail = jwtService.getUsernameFromToken(jwt);
//        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
//            throw new RuntimeException(e);
//        }
//        if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
//            UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);
//            try {
//                if (jwtService.validateToken(authHeader, userDetails)) {
//                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
//                            userDetails,
//                            null,
//                            userDetails.getAuthorities()
//                    );
//                    authToken.setDetails(
//                            new WebAuthenticationDetailsSource().buildDetails(request)
//                    );
//                    SecurityContextHolder.getContext().setAuthentication(authToken);
//                }
//            } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
//                throw new RuntimeException(e);
//            }
//        }
//        filterChain.doFilter(request, response);
//    }
}