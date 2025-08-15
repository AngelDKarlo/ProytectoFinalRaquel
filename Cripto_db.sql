-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Servidor: localhost
-- Tiempo de generación: 14-08-2025 a las 22:24:13
-- Versión del servidor: 8.0.42-0ubuntu0.22.04.1
-- Versión de PHP: 8.1.2-1ubuntu2.22

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Base de datos: `Cripto_db`
--

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `cripto`
--

CREATE TABLE `cripto` (
  `id` int NOT NULL,
  `nombre` varchar(255) NOT NULL,
  `simbolo` varchar(255) NOT NULL,
  `precio` decimal(20,8) DEFAULT NULL,
  `descripcion` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Volcado de datos para la tabla `cripto`
--

INSERT INTO `cripto` (`id`, `nombre`, `simbolo`, `precio`, `descripcion`) VALUES
(1, 'Zorcoin', 'ZOR', 12.58000000, 'Un activo digital diseñado para pagos rápidos y seguros.'),
(2, 'Nebulium', 'NEB', 12.58000000, 'Plataforma blockchain enfocada en contratos inteligentes escalables.'),
(3, 'Lumera', 'LUM', 12.58000000, 'Red descentralizada orientada a la interoperabilidad entre cadenas.');

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `ordenes`
--

CREATE TABLE `ordenes` (
  `id_orden` int NOT NULL,
  `id_usuario` int NOT NULL,
  `id_cripto` int NOT NULL,
  `tipo_orden` varchar(10) NOT NULL,
  `cantidad` decimal(20,8) NOT NULL,
  `precio_por_unidad` decimal(20,8) NOT NULL,
  `estado` varchar(20) DEFAULT 'abierta',
  `fecha_creacion` timestamp NULL DEFAULT CURRENT_TIMESTAMP
) ;

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `portafolio_usuario`
--

CREATE TABLE `portafolio_usuario` (
  `id` int NOT NULL,
  `id_usuario` int NOT NULL,
  `saldo_usd` decimal(20,2) NOT NULL DEFAULT '10000.00',
  `fecha_creacion` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `fecha_actualizacion` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Volcado de datos para la tabla `portafolio_usuario`
--

INSERT INTO `portafolio_usuario` (`id`, `id_usuario`, `saldo_usd`, `fecha_creacion`, `fecha_actualizacion`) VALUES
(1, 6, 10000.00, '2025-08-14 00:53:08', '2025-08-14 00:53:08');

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `price_history`
--

CREATE TABLE `price_history` (
  `id` int NOT NULL,
  `crypto_id` int NOT NULL,
  `precio` decimal(20,8) NOT NULL,
  `volumen` decimal(20,8) DEFAULT NULL,
  `precio_apertura` decimal(20,8) DEFAULT NULL,
  `precio_maximo` decimal(20,8) DEFAULT NULL,
  `precio_minimo` decimal(20,8) DEFAULT NULL,
  `precio_cierre` decimal(20,8) DEFAULT NULL,
  `timestamp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `intervalo` varchar(10) DEFAULT '5s'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `transacciones_ejecutadas`
--

CREATE TABLE `transacciones_ejecutadas` (
  `id_transaccion` int NOT NULL,
  `id_orden_compra` int NOT NULL,
  `id_orden_venta` int NOT NULL,
  `id_cripto` int NOT NULL,
  `cantidad` decimal(20,8) NOT NULL,
  `precio_ejecucion` decimal(20,8) NOT NULL,
  `fecha_ejecucion` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `comision` decimal(10,8) DEFAULT NULL,
  `id_usuario` int NOT NULL,
  `tipo_transaccion` varchar(10) NOT NULL
) ;

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `users`
--

CREATE TABLE `users` (
  `id` int NOT NULL,
  `nombre_usuario` varchar(255) NOT NULL,
  `nombre_completo` varchar(255) NOT NULL,
  `contraseña_hash` varchar(255) NOT NULL,
  `correo_electronico` varchar(255) NOT NULL,
  `fecha_registro` date DEFAULT NULL,
  `fecha_nac` date DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Volcado de datos para la tabla `users`
--

INSERT INTO `users` (`id`, `nombre_usuario`, `nombre_completo`, `contraseña_hash`, `correo_electronico`, `fecha_registro`, `fecha_nac`) VALUES
(1, 'hola', 'pepito', '$2a$10$AauUjG67YfNyu.lCF3Jt6OfoHOdfnphlEKHynfHWb11xfQOYp898y', 'admin', '2005-05-26', '2025-08-12'),
(4, 'a', 'pepito', '$2a$10$C6.MS.wAaevSm7aU2mCAF.qTeTzTfjyWhef59DVwzviwX3N5hxGVC', 'felipe', '2005-05-26', '2025-08-12'),
(5, 'b', 'pepito', '$2a$10$.rSJ82jcPNccAdqrAVXdpuRkjxsvll3qg8i5HDCJjAk8dthmgNKBW', 'juan', '2005-05-26', '2025-08-12'),
(6, 'testuser', 'Usuario de Prueba', '$2a$10$dummyhash', 'test@example.com', NULL, '1990-01-01');

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `wallets`
--

CREATE TABLE `wallets` (
  `id_billetera` int NOT NULL,
  `id_usuario` int NOT NULL,
  `id_cripto` int NOT NULL,
  `saldo` decimal(20,8) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Índices para tablas volcadas
--

--
-- Indices de la tabla `cripto`
--
ALTER TABLE `cripto`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `simbolo` (`simbolo`);

--
-- Indices de la tabla `ordenes`
--
ALTER TABLE `ordenes`
  ADD PRIMARY KEY (`id_orden`),
  ADD KEY `id_cripto` (`id_cripto`),
  ADD KEY `idx_ordenes_usuario` (`id_usuario`),
  ADD KEY `idx_ordenes_estado` (`estado`),
  ADD KEY `idx_ordenes_tipo` (`tipo_orden`);

--
-- Indices de la tabla `portafolio_usuario`
--
ALTER TABLE `portafolio_usuario`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `id_usuario` (`id_usuario`);

--
-- Indices de la tabla `price_history`
--
ALTER TABLE `price_history`
  ADD PRIMARY KEY (`id`),
  ADD KEY `idx_crypto_timestamp` (`crypto_id`,`timestamp`),
  ADD KEY `idx_timestamp` (`timestamp`),
  ADD KEY `fk_price_history_crypto` (`crypto_id`);

--
-- Indices de la tabla `transacciones_ejecutadas`
--
ALTER TABLE `transacciones_ejecutadas`
  ADD PRIMARY KEY (`id_transaccion`),
  ADD KEY `id_orden_compra` (`id_orden_compra`),
  ADD KEY `id_orden_venta` (`id_orden_venta`),
  ADD KEY `id_cripto` (`id_cripto`),
  ADD KEY `idx_transacciones_fecha` (`fecha_ejecucion`),
  ADD KEY `idx_transacciones_usuario` (`id_usuario`),
  ADD KEY `idx_transacciones_tipo` (`tipo_transaccion`);

--
-- Indices de la tabla `users`
--
ALTER TABLE `users`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `nombre_usuario` (`nombre_usuario`),
  ADD UNIQUE KEY `correo_electronico` (`correo_electronico`);

--
-- Indices de la tabla `wallets`
--
ALTER TABLE `wallets`
  ADD PRIMARY KEY (`id_billetera`),
  ADD UNIQUE KEY `id_usuario` (`id_usuario`,`id_cripto`),
  ADD KEY `idx_wallets_usuario` (`id_usuario`),
  ADD KEY `idx_wallets_cripto` (`id_cripto`),
  ADD KEY `idx_wallets_usuario_cripto` (`id_usuario`,`id_cripto`);

--
-- AUTO_INCREMENT de las tablas volcadas
--

--
-- AUTO_INCREMENT de la tabla `cripto`
--
ALTER TABLE `cripto`
  MODIFY `id` int NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=10;

--
-- AUTO_INCREMENT de la tabla `ordenes`
--
ALTER TABLE `ordenes`
  MODIFY `id_orden` int NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT de la tabla `portafolio_usuario`
--
ALTER TABLE `portafolio_usuario`
  MODIFY `id` int NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=2;

--
-- AUTO_INCREMENT de la tabla `price_history`
--
ALTER TABLE `price_history`
  MODIFY `id` int NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT de la tabla `transacciones_ejecutadas`
--
ALTER TABLE `transacciones_ejecutadas`
  MODIFY `id_transaccion` int NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT de la tabla `users`
--
ALTER TABLE `users`
  MODIFY `id` int NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=7;

--
-- AUTO_INCREMENT de la tabla `wallets`
--
ALTER TABLE `wallets`
  MODIFY `id_billetera` int NOT NULL AUTO_INCREMENT;

--
-- Restricciones para tablas volcadas
--

--
-- Filtros para la tabla `ordenes`
--
ALTER TABLE `ordenes`
  ADD CONSTRAINT `ordenes_ibfk_1` FOREIGN KEY (`id_usuario`) REFERENCES `users` (`id`),
  ADD CONSTRAINT `ordenes_ibfk_2` FOREIGN KEY (`id_cripto`) REFERENCES `cripto` (`id`);

--
-- Filtros para la tabla `portafolio_usuario`
--
ALTER TABLE `portafolio_usuario`
  ADD CONSTRAINT `portafolio_usuario_ibfk_1` FOREIGN KEY (`id_usuario`) REFERENCES `users` (`id`);

--
-- Filtros para la tabla `price_history`
--
ALTER TABLE `price_history`
  ADD CONSTRAINT `fk_price_history_crypto` FOREIGN KEY (`crypto_id`) REFERENCES `cripto` (`id`) ON DELETE CASCADE;

--
-- Filtros para la tabla `transacciones_ejecutadas`
--
ALTER TABLE `transacciones_ejecutadas`
  ADD CONSTRAINT `transacciones_ejecutadas_ibfk_1` FOREIGN KEY (`id_orden_compra`) REFERENCES `ordenes` (`id_orden`),
  ADD CONSTRAINT `transacciones_ejecutadas_ibfk_2` FOREIGN KEY (`id_orden_venta`) REFERENCES `ordenes` (`id_orden`),
  ADD CONSTRAINT `transacciones_ejecutadas_ibfk_3` FOREIGN KEY (`id_cripto`) REFERENCES `cripto` (`id`);

--
-- Filtros para la tabla `wallets`
--
ALTER TABLE `wallets`
  ADD CONSTRAINT `wallets_ibfk_1` FOREIGN KEY (`id_usuario`) REFERENCES `users` (`id`),
  ADD CONSTRAINT `wallets_ibfk_2` FOREIGN KEY (`id_cripto`) REFERENCES `cripto` (`id`);
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
