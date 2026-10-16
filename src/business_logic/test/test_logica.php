<?php
// ---------------------------------------------------------------------
// Fichero:     test_logica.php
// Descripción: Tests automáticos de Logica y AlmacenMediciones. Usan una
//              base de datos SQLite en memoria con la misma tabla que
//              MySQL, así que no hace falta servidor ni base de datos real.
//              Se ejecutan desde src/business_logic con:
//                  php test/test_logica.php
// Copyright:   (c) 2026 Matilde Calleja
// Fecha:       15/10/2026
// Autor:       Matilde Calleja
// Aportación:  Tests generados con IA a partir del diseño
//              (business_logic_design.md) y revisados por la autora.
// ---------------------------------------------------------------------

require_once __DIR__ . '/../Logica.php';

$fallos = 0;
$total = 0;

// --------------------------------------------------------------
// condicion: B, nombre: Text --> comprobar() -->
// --------------------------------------------------------------
function comprobar($condicion, $nombre)
{
    global $fallos, $total;
    $total++;
    echo ($condicion ? 'OK     ' : 'FALLO  ') . $nombre . PHP_EOL;
    if (!$condicion) {
        $fallos++;
    }
}

// --------------------------------------------------------------
// funcion: () --> lanzaArgumentoInvalido() --> B
// true si funcion() lanza InvalidArgumentException; false si no
// lanza nada o lanza otro error.
// --------------------------------------------------------------
function lanzaArgumentoInvalido(callable $funcion)
{
    try {
        $funcion();
        return false;
    } catch (InvalidArgumentException $e) {
        return true;
    } catch (Throwable $e) {
        return false;
    }
}

// --------------------------------------------------------------
// Logica <-- logicaConBaseDeDatosVacia() <--
// Tabla equivalente a src/database/crear_bd.sql, en SQLite en memoria.
// --------------------------------------------------------------
function logicaConBaseDeDatosVacia()
{
    $bd = new PDO('sqlite::memory:');
    $bd->exec(
        'CREATE TABLE medidas (
            id       INTEGER PRIMARY KEY AUTOINCREMENT,
            tipo     INTEGER NOT NULL CHECK (tipo IN (11, 12, 13)),
            valor    INTEGER NOT NULL,
            contador INTEGER NOT NULL,
            fecha    TEXT    NOT NULL
        )'
    );
    return new Logica(new AlmacenMediciones($bd));
}

// --- Base de datos vacía -------------------------------------------
$logica = logicaConBaseDeDatosVacia();
comprobar($logica->obtenerUltimaMedida() === null, 'sin medidas, la última es null');

// --- Guardar y recuperar -------------------------------------------
$guardada = $logica->guardarMedida(11, 123, 5);
comprobar($guardada['id'] === 1, 'la primera medida recibe el id 1');
comprobar($guardada['tipo'] === 11 && $guardada['valor'] === 123 && $guardada['contador'] === 5,
    'guardarMedida devuelve lo que ha guardado');

$ultima = $logica->obtenerUltimaMedida();
comprobar($ultima['tipo'] === 11, 'se recupera el tipo');
comprobar($ultima['valor'] === 123, 'se recupera el valor (medida ficticia de la demo)');
comprobar($ultima['contador'] === 5, 'se recupera el contador');
comprobar(strtotime($ultima['fecha']) !== false, 'la fecha está en un formato válido');

// --- La más reciente -------------------------------------------------
$logica->guardarMedida(11, 457, 6);
$logica->guardarMedida(12, 20, 7);
$ultima = $logica->obtenerUltimaMedida();
comprobar($ultima['id'] === 3 && $ultima['tipo'] === 12 && $ultima['valor'] === 20,
    'obtenerUltimaMedida devuelve la última que ha llegado');

// --- Valores límite y negativos ----------------------------------------
$logica = logicaConBaseDeDatosVacia();
$logica->guardarMedida(12, -12, 0);
comprobar($logica->obtenerUltimaMedida()['valor'] === -12, 'valor negativo');
$logica->guardarMedida(11, -32768, 0);
comprobar($logica->obtenerUltimaMedida()['valor'] === -32768, 'valor mínimo -32768');
$logica->guardarMedida(11, 32767, 255);
$ultima = $logica->obtenerUltimaMedida();
comprobar($ultima['valor'] === 32767 && $ultima['contador'] === 255, 'valor máximo y contador 255');

// --- Textos con números (como llegan a veces del JSON) ------------------
$guardada = $logica->guardarMedida('13', '40', '9');
comprobar($guardada['tipo'] === 13 && $guardada['valor'] === 40, 'acepta números escritos como texto');

// --- Datos no válidos ----------------------------------------------------
$logica = logicaConBaseDeDatosVacia();
comprobar(lanzaArgumentoInvalido(function () use ($logica) { $logica->guardarMedida(99, 1, 1); }),
    'rechaza un tipo que no existe');
comprobar(lanzaArgumentoInvalido(function () use ($logica) { $logica->guardarMedida(14, 1, 1); }),
    'rechaza el tipo 14 (todavía no existe)');
comprobar(lanzaArgumentoInvalido(function () use ($logica) { $logica->guardarMedida(11, 32768, 1); }),
    'rechaza un valor demasiado grande');
comprobar(lanzaArgumentoInvalido(function () use ($logica) { $logica->guardarMedida(11, -32769, 1); }),
    'rechaza un valor demasiado pequeño');
comprobar(lanzaArgumentoInvalido(function () use ($logica) { $logica->guardarMedida(11, 1, 256); }),
    'rechaza un contador mayor que 255');
comprobar(lanzaArgumentoInvalido(function () use ($logica) { $logica->guardarMedida(11, 1, -1); }),
    'rechaza un contador negativo');
comprobar(lanzaArgumentoInvalido(function () use ($logica) { $logica->guardarMedida(null, 1, 1); }),
    'rechaza un tipo que falta');
comprobar(lanzaArgumentoInvalido(function () use ($logica) { $logica->guardarMedida(11, 'abc', 1); }),
    'rechaza un valor que no es un número');
comprobar(lanzaArgumentoInvalido(function () use ($logica) { $logica->guardarMedida(11, 1.5, 1); }),
    'rechaza un valor con decimales');
comprobar(lanzaArgumentoInvalido(function () use ($logica) { $logica->guardarMedida(11, true, 1); }),
    'rechaza un booleano');
comprobar($logica->obtenerUltimaMedida() === null, 'los datos no válidos no se guardan');

// --- Fallo de la base de datos -------------------------------------------
$sinTabla = new Logica(new AlmacenMediciones(new PDO('sqlite::memory:')));
$lanzaPDOException = false;
try {
    $sinTabla->guardarMedida(11, 1, 1);
} catch (PDOException $e) {
    $lanzaPDOException = true;
}
comprobar($lanzaPDOException, 'un fallo de la base de datos se convierte en error');

echo PHP_EOL . ($fallos === 0 ? 'TODO OK' : 'HAY FALLOS') . " ($total tests, $fallos fallos)" . PHP_EOL;
exit($fallos === 0 ? 0 : 1);