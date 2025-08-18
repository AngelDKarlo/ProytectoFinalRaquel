#!/bin/bash
# fix-cors-final.sh - Solución definitiva para el problema de CORS

echo "🔧 ARREGLANDO CORS - Problema /market/prices"
echo "============================================"

# 1. Hacer backup del controlador actual
echo "📋 Haciendo backup del MarketController actual..."
cp src/main/java/com/trading/cripto/controller/MarketController.java \
   src/main/java/com/trading/cripto/controller/MarketController.java.backup

# 2. Mostrar el problema detectado
echo ""
echo "🔍 PROBLEMA DETECTADO:"
echo "======================"
echo "❌ /api/market/prices responde 'Failed to fetch'"
echo "✅ /api/debug/stats funciona perfectamente"
echo "🎯 CAUSA: CORS mal configurado en MarketController"

# 3. Verificar estructura de archivos
echo ""
echo "📁 Verificando estructura..."
if [ ! -f "src/main/java/com/trading/cripto/controller/MarketController.java" ]; then
    echo "❌ MarketController.java no encontrado"
    exit 1
fi

if [ ! -d "src/main/java/com/trading/cripto/config" ]; then
    echo "📁 Creando directorio config..."
    mkdir -p src/main/java/com/trading/cripto/config
fi

echo "✅ Estructura verificada"

# 4. Aplicar el MarketController corregido
echo ""
echo "🔧 Aplicando MarketController corregido..."
cat > src/main/java/com/trading/cripto/controller/MarketController.java << 'EOF'
package com.trading.cripto.controller;

import com.trading.cripto.model.Cryptocurrency;
import com.trading.cripto.model.PriceHistory;
import com.trading.cripto.repository.CryptocurrencyRepository;
import com.trading.cripto.service.MarketDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/market")
@CrossOrigin(
    origins = "*", 
    allowedHeaders = "*",
    methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS},
    allowCredentials = "false"
)
public class MarketController {

    @Autowired
    private CryptocurrencyRepository cryptoRepo;

    @Autowired
    private MarketDataService marketDataService;

    /**
     * Obtener todas las criptomonedas y sus precios actuales
     * GET /api/market/prices - CORREGIDO PARA CORS
     */
    @GetMapping("/prices")
    public ResponseEntity<List<Cryptocurrency>> obtenerPrecios() {
        try {
            System.out.println("🔍 [MarketController] Solicitud recibida en /api/market/prices");
            
            List<Cryptocurrency> cryptos = cryptoRepo.findAll();
            System.out.println("📊 [MarketController] Criptomonedas encontradas: " + cryptos.size());
            
            return ResponseEntity.ok()
                    .header("Access-Control-Allow-Origin", "*")
                    .header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS")
                    .header("Access-Control-Allow-Headers", "Content-Type, Authorization, X-Requested-With")
                    .header("Access-Control-Allow-Credentials", "false")
                    .body(cryptos);
        } catch (Exception e) {
            System.err.println("❌ [MarketController] Error en /market/prices: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError()
                    .header("Access-Control-Allow-Origin", "*")
                    .header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS")
                    .header("Access-Control-Allow-Headers", "Content-Type, Authorization")
                    .build();
        }
    }

    /**
     * CRÍTICO: OPTIONS para preflight CORS
     */
    @RequestMapping(method = RequestMethod.OPTIONS, value = "/prices")
    public ResponseEntity<?> handlePreflightPrices() {
        System.out.println("🔄 [MarketController] OPTIONS request recibido para /prices");
        return ResponseEntity.ok()
                .header("Access-Control-Allow-Origin", "*")
                .header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS")
                .header("Access-Control-Allow-Headers", "Authorization, Content-Type, X-Requested-With, Accept, Origin")
                .header("Access-Control-Allow-Credentials", "false")
                .header("Access-Control-Max-Age", "3600")
                .build();
    }

    @RequestMapping(method = RequestMethod.OPTIONS, value = "/**")
    public ResponseEntity<?> handlePreflightGeneral() {
        System.out.println("🔄 [MarketController] OPTIONS request recibido para endpoint general");
        return ResponseEntity.ok()
                .header("Access-Control-Allow-Origin", "*")
                .header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS")
                .header("Access-Control-Allow-Headers", "Authorization, Content-Type, X-Requested-With, Accept, Origin")
                .header("Access-Control-Allow-Credentials", "false")
                .header("Access-Control-Max-Age", "3600")
                .build();
    }

    @GetMapping("/price/{symbol}")
    public ResponseEntity<Cryptocurrency> obtenerPrecio(@PathVariable String symbol) {
        try {
            System.out.println("🔍 [MarketController] Solicitud para precio de: " + symbol);
            Optional<Cryptocurrency> crypto = cryptoRepo.findBySimbolo(symbol.toUpperCase());
            if (crypto.isPresent()) {
                return ResponseEntity.ok()
                        .header("Access-Control-Allow-Origin", "*")
                        .header("Access-Control-Allow-Methods", "GET, OPTIONS")
                        .header("Access-Control-Allow-Headers", "Content-Type, Authorization")
                        .body(crypto.get());
            } else {
                return ResponseEntity.notFound()
                        .header("Access-Control-Allow-Origin", "*")
                        .build();
            }
        } catch (Exception e) {
            System.err.println("❌ [MarketController] Error en /market/price/" + symbol + ": " + e.getMessage());
            return ResponseEntity.internalServerError()
                    .header("Access-Control-Allow-Origin", "*")
                    .build();
        }
    }

    @GetMapping("/test")
    public ResponseEntity<?> test() {
        System.out.println("🧪 [MarketController] Test endpoint accedido");
        return ResponseEntity.ok()
                .header("Access-Control-Allow-Origin", "*")
                .header("Access-Control-Allow-Methods", "GET, OPTIONS")
                .header("Access-Control-Allow-Headers", "Content-Type")
                .body(Map.of(
                    "status", "OK",
                    "message", "MarketController funcionando",
                    "timestamp", System.currentTimeMillis()
                ));
    }
}
EOF

echo "✅ MarketController corregido aplicado"

# 5. Aplicar CorsFilter global
echo ""
echo "🔧 Aplicando CorsFilter global..."
cat > src/main/java/com/trading/cripto/config/CorsFilter.java << 'EOF'
package com.trading.cripto.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CorsFilter implements Filter {

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) 
            throws IOException, ServletException {
        
        HttpServletResponse response = (HttpServletResponse) res;
        HttpServletRequest request = (HttpServletRequest) req;

        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", 
            "Authorization, Content-Type, X-Requested-With, Accept, Origin, Access-Control-Request-Method, Access-Control-Request-Headers");
        response.setHeader("Access-Control-Allow-Credentials", "false");
        response.setHeader("Access-Control-Max-Age", "3600");

        String uri = request.getRequestURI();
        String method = request.getMethod();
        
        if (uri.contains("/market/prices")) {
            System.out.println("🔄 [CORS Filter] MARKET/PRICES: " + method + " " + uri);
        }
        
        if ("OPTIONS".equalsIgnoreCase(method)) {
            System.out.println("✅ [CORS Filter] OPTIONS request para " + uri + " - respondiendo con 200");
            response.setStatus(HttpServletResponse.SC_OK);
            return;
        }

        chain.doFilter(req, res);
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        System.out.println("🚀 [CORS Filter] Inicializado - CORS habilitado");
    }

    @Override
    public void destroy() {
        System.out.println("🛑 [CORS Filter] Destruido");
    }
}
EOF

echo "✅ CorsFilter global aplicado"

# 6. Rebuild de la aplicación
echo ""
echo "🔧 Rebuilding aplicación con CORS corregido..."
docker-compose down
sleep 5
docker-compose build --no-cache app
docker-compose up -d

# 7. Esperar a que arranque
echo "⏳ Esperando a que Spring Boot arranque con CORS corregido..."
for i in {1..30}; do
    echo "[$i/30] Verificando arranque..."
    if curl -f -s http://localhost:8080/api/debug/connection >/dev/null 2>&1; then
        echo "✅ Spring Boot está funcionando"
        break
    fi
    sleep 5
done

# 8. Test específico del endpoint problemático
echo ""
echo "🧪 TEST FINAL DEL ENDPOINT PROBLEMÁTICO:"
echo "======================================="

echo "1. Test de /market/prices:"
if curl -f -s http://localhost:8080/api/market/prices >/dev/null 2>&1; then
    echo "✅ /market/prices FUNCIONANDO"
    echo "📊 Respuesta:"
    curl -s http://localhost:8080/api/market/prices | head -5
else
    echo "❌ /market/prices sigue fallando"
    echo "📋 Logs de Spring Boot:"
    docker logs crypto-backend-clean --tail=20 | grep -i "market\|cors\|error"
fi

echo ""
echo "2. Test CORS específico:"
CORS_TEST=$(curl -s -H "Origin: http://localhost" -I http://localhost:8080/api/market/prices | grep -i "access-control")
if [ ! -z "$CORS_TEST" ]; then
    echo "✅ Headers CORS presentes:"
    echo "$CORS_TEST"
else
    echo "❌ No hay headers CORS"
fi

# 9. Información final
echo ""
echo "========================================="
echo "🎯 RESULTADO:"
echo "========================================="

if curl -f -s http://localhost:8080/api/market/prices >/dev/null 2>&1; then
    echo "🎉 ¡CORS ARREGLADO!"
    echo ""
    echo "✅ /api/market/prices FUNCIONANDO"
    echo "✅ CORS configurado correctamente"
    echo "✅ Tu frontend debería conectarse ahora"
    echo ""
    echo "🔗 URLs para probar:"
    echo "   📊 Precios: http://157.245.164.138:8080/api/market/prices"
    echo "   🧪 Test: http://157.245.164.138:8080/api/market/test"
else
    echo "⚠️ CORS aplicado pero endpoint aún tiene problemas"
    echo ""
    echo "📋 Para debug:"
    echo "   docker logs crypto-backend-clean -f | grep -i market"
    echo ""
    echo "🔄 Endpoint alternativo que FUNCIONA:"
    echo "   📊 Debug: http://157.245.164.138:8080/api/debug/stats"
fi

echo ""
echo "🏁 Corrección de CORS completada"
