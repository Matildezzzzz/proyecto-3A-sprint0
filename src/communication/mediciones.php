<?php
// ---------------------------------------------------------------------
// Fichero:     mediciones.php
// Descripción: API REST. Solo traduce peticiones HTTP a llamadas a la
//              lógica del negocio y su resultado a respuestas HTTP.
//                POST /api/mediciones.php  {tipo, valor, contador}
//                     --> 201 {ok, mensaje, medicion}
//                GET  /api/mediciones.php
//                     --> 200 {id, tipo, valor, contador, fecha}
//                         404 si todavía no hay medidas
// Copyright:   (c) 2026 Matilde Calleja
// Fecha:       14/10/2026
// Autor:       Matilde Calleja
// Aportación:  Diseño propio; código generado con IA a partir del diseño
//              y revisado por la autora.
// ---------------------------------------------------------------------

require_once __DIR__ . '/../business_logic/Logica.php';

header('Content-Type: application/json; charset=utf-8');

// --------------------------------------------------------------
// codigo: N, datos: {..} --> responder() -->
// --------------------------------------------------------------
function responder($codigo, $datos)
{
    http_response_code($codigo);
    echo json_encode($datos, JSON_UNESCAPED_UNICODE);
    exit;
}

try {
    $config = require __DIR__ . '/config.php';
    $bd = new PDO($config['dsn'], $config['usuario'], $config['contrasena']);
    $logica = new Logica(new AlmacenMediciones($bd));

    switch ($_SERVER['REQUEST_METHOD']) {

        // GET: devolver la última medida
        case 'GET':
            $medida = $logica->obtenerUltimaMedida();
            if ($medida === null) {
                responder(404, ['ok' => false, 'mensaje' => 'Todavía no hay mediciones']);
            }
            responder(200, $medida);

        // POST: guardar una medida
        case 'POST':
            $datos = json_decode(file_get_contents('php://input'), true);
            if (!is_array($datos)) {
                responder(400, ['ok' => false, 'mensaje' => 'JSON inválido']);
            }

            $medida = $logica->guardarMedida(
                $datos['tipo'] ?? null,
                $datos['valor'] ?? null,
                $datos['contador'] ?? null
            );
            responder(201, ['ok' => true, 'mensaje' => 'Medición recibida', 'medicion' => $medida]);

        default:
            header('Allow: GET, POST');
            responder(405, ['ok' => false, 'mensaje' => 'Método no permitido']);
    }

} catch (InvalidArgumentException $e) {
    responder(400, ['ok' => false, 'mensaje' => $e->getMessage()]);

} catch (Throwable $e) {
    // El detalle va al log del servidor, no a la respuesta: los errores
    // de la base de datos pueden contener el usuario o el host.
    error_log('AppBiom: ' . $e->getMessage());
    responder(500, ['ok' => false, 'mensaje' => 'Error interno del servidor']);
}