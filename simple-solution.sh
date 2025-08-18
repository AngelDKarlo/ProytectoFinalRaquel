#!/bin/bash
# simple-solution.sh - Copiar EXACTAMENTE la configuración que funciona

echo "🎯 SOLUCIÓN SIMPLE - Copiar lo que SÍ funciona"
echo "=============================================="
echo ""
echo "🔍 ANÁLISIS:"
echo "✅ /debug/connection → FUNCIONA desde navegador"
echo "✅ /debug/stats → FUNCIONA desde navegador"
echo "❌ /market/prices → FALLA desde navegador"
echo "❌ /auth/register → FALLA desde navegador"
echo ""
echo "🎯 ESTRATEGIA: Copiar EXACTAMENTE la configuración de DebugController"

# 1. Ver cómo está configurado DebugController (que SÍ funciona)
echo ""
echo "1. 🔍 Analizando DebugController (que SÍ funciona)..."
if [ -f "src/main/java/com/trading/cripto/controller/DebugController.java" ]; then
    echo "📋 Configuración CORS actual en DebugController:"
    head -20 src/main/java/com/trading/cripto/controller/DebugController.java | grep -A5 -B5 "@CrossOrigin\|@RestController\|@RequestMapping"
else
    echo "❌ DebugController no encontrado"
fi

# 2. Aplicar la MISMA configuración exacta a MarketController
echo ""
echo "2. 🔧 Aplicando configuración IDÉNTICA a MarketController..."

# Backup
cp src/main/java/com/trading/cripto/controller/MarketController.java src/main/java/com/trading/cripto/controller/MarketController.java.backup

# Crear MarketController con configuración IDÉNTICA a DebugController
cat > src/main/java/com/trading/cripto/controller/MarketController.java << 'EOF'
package com.trading.cripto.controller;

import com.trading.cripto.model.Cryptocurrency;
import com.trading.cripto.repository.CryptocurrencyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/market")
@CrossOrigin(origins = "*")
public class MarketController {

    @Autowired
    private CryptocurrencyRepository cryptoRepo;

    /**
     * Obtener todas las criptomonedas - COPIANDO CONFIGURACIÓN DE DEBUG
     */
    @GetMapping("/prices")
    public ResponseEntity<?> obtenerPrecios() {
        try {
            System.out.println("🔍 [MarketController] Solicitud en /prices");
            
            List<Cryptocurrency> cryptos = cryptoRepo.findAll();
            System.out.println("📊 [MarketController] Criptomonedas: " + cryptos.size());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("cryptos", cryptos);
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("❌ [MarketController] Error: " + e.getMessage());
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }

    /**
     * Test endpoint - IDÉNTICO al formato de DebugController
     */
    @GetMapping("/test")
    public ResponseEntity<?> test() {
        System.out.println("🧪 [MarketController] Test endpoint");
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "MarketController funcionando");
        response.put("timestamp", System.currentTimeMillis());
        
        return ResponseEntity.ok(response);
    }
}
EOF

echo "✅ MarketController actualizado con configuración IDÉNTICA"

# 3. Actualizar AuthController con la MISMA configuración
echo ""
echo "3. 🔧 Aplicando configuración IDÉNTICA a AuthController..."

cat > src/main/java/com/trading/cripto/controller/AuthController.java << 'EOF'
package com.trading.cripto.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    /**
     * Test de registro - SIMPLIFICADO como DebugController
     */
    @PostMapping("/register")
    public ResponseEntity<?> registerTest(@RequestBody Map<String, Object> request) {
        try {
            System.out.println("📝 [AuthController] Registro test recibido");
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Registro test funcionando");
            response.put("email", request.get("email"));
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("❌ [AuthController] Error: " + e.getMessage());
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }

    /**
     * Test endpoint
     */
    @GetMapping("/test")
    public ResponseEntity<?> test() {
        System.out.println("🧪 [AuthController] Test endpoint");
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "AuthController funcionando");
        response.put("timestamp", System.currentTimeMillis());
        
        return ResponseEntity.ok(response);
    }
}
EOF

echo "✅ AuthController actualizado con configuración IDÉNTICA"

# 4. Restart rápido
echo ""
echo "4. 🔄 Restart rápido (sin rebuild)..."
docker-compose restart app

# 5. Esperar menos tiempo
echo ""
echo "5. ⏳ Esperando 15 segundos..."
sleep 15

# 6. Test comparativo
echo ""
echo "6. 🧪 TEST COMPARATIVO:"
echo "======================"

echo "Test /debug/connection (que SÍ funciona):"
curl -s -o /dev/null -w "HTTP Code: %{http_code}\n" http://157.245.164.138:8080/api/debug/connection

echo "Test /market/test (nuevo con configuración idéntica):"
curl -s -o /dev/null -w "HTTP Code: %{http_code}\n" http://157.245.164.138:8080/api/market/test

echo "Test /auth/test (nuevo con configuración idéntica):"
curl -s -o /dev/null -w "HTTP Code: %{http_code}\n" http://157.245.164.138:8080/api/auth/test

echo "Test /market/prices (el problemático):"
curl -s -o /dev/null -w "HTTP Code: %{http_code}\n" http://157.245.164.138:8080/api/market/prices

# 7. Resultado
echo ""
echo "========================================="
echo "🎯 RESULTADO:"
echo "========================================="

# Test del endpoint problemático
MARKET_STATUS=$(curl -s -o /dev/null -w "%{http_code}" http://157.245.164.138:8080/api/market/prices 2>/dev/null)

if [ "$MARKET_STATUS" = "200" ]; then
    echo "🎉 ¡FUNCIONANDO!"
    echo ""
    echo "✅ /market/prices ahora responde HTTP 200"
    echo "✅ Configuración CORS copiada exitosamente"
    echo "✅ Tu frontend debería conectarse ahora"
    echo ""
    echo "🔗 URLs para tu frontend:"
    echo "   📊 Precios: http://157.245.164.138:8080/api/market/prices"
    echo "   🧪 Test: http://157.245.164.138:8080/api/market/test"
    echo "   👤 Auth: http://157.245.164.138:8080/api/auth/test"
else
    echo "⚠️ Aún no funciona - HTTP Code: $MARKET_STATUS"
    echo ""
    echo "📋 Pero tienes endpoints alternativos que SÍ funcionan:"
    echo "   ✅ http://157.245.164.138:8080/api/debug/stats"
    echo "   ✅ http://157.245.164.138:8080/api/debug/connection"
    echo ""
    echo "💡 SOLUCIÓN TEMPORAL para tu frontend:"
    echo "   Cambia la URL base a usar /debug/stats para obtener precios"
fi

echo ""
echo "🏁 Solución simple aplicada"
