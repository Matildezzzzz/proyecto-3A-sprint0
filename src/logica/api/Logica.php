<?php
// ---------------------------------------------------------------------
// Fichero:     Logica.php
// Descripción: Lógica del negocio. Valida y guarda medidas y devuelve
//              la última. No sabe nada de HTTP.
// Copyright:   (c) 2026 Matilde Calleja
// Fecha:       14/10/2026
// Autor:       Matilde Calleja
// Aportación:  Diseño propio; código generado con IA a partir del diseño
//              y revisado por la autora.
// ---------------------------------------------------------------------

require_once __DIR__ . '/AlmacenMediciones.php';

class Logica
{
    const TIPOS_VALIDOS = [11, 12, 13];   // CO2, temperatura y ruido

    private $almacen;

    // --------------------------------------------------------------
    // almacen: AlmacenMediciones --> Logica() -->
    // --------------------------------------------------------------
    public function __construct(AlmacenMediciones $almacen)
    {
        $this->almacen = $almacen;
    }

    // --------------------------------------------------------------
    // tipo: N, valor: Z, contador: N --> guardarMedida() --> Medida
    //
    // 1. tipo tiene que ser 11, 12 o 13.
    // 2. valor tiene que ser un entero entre -32768 y 32767.
    // 3. contador tiene que ser un entero entre 0 y 255.
    //    Si algo falla: error InvalidArgumentException.
    // 4. fecha = fecha y hora actual.
    // 5. id = almacen.insertar(tipo, valor, contador, fecha)
    // 6. Devolver la medida guardada con su id.
    // --------------------------------------------------------------
    public function guardarMedida($tipo, $valor, $contador)
    {
        $tipo = $this->entero($tipo, 'tipo');
        $valor = $this->entero($valor, 'valor');
        $contador = $this->entero($contador, 'contador');

        if (!in_array($tipo, self::TIPOS_VALIDOS, true)) {
            throw new InvalidArgumentException('tipo no válido: ' . $tipo);
        }
        if ($valor < -32768 || $valor > 32767) {
            throw new InvalidArgumentException('valor fuera de rango: ' . $valor);
        }
        if ($contador < 0 || $contador > 255) {
            throw new InvalidArgumentException('contador fuera de rango: ' . $contador);
        }

        $medida = [
            'tipo' => $tipo,
            'valor' => $valor,
            'contador' => $contador,
            'fecha' => date('c'),
        ];

        $id = $this->almacen->insertar($medida);

        return array_merge(['id' => $id], $medida);
    }

    // --------------------------------------------------------------
    // Medida | null <-- obtenerUltimaMedida() <--
    // --------------------------------------------------------------
    public function obtenerUltimaMedida()
    {
        return $this->almacen->ultima();
    }

    // --------------------------------------------------------------
    // dato: ?, nombre: Text --> entero() --> Z
    // Error si dato no es un número entero.
    // --------------------------------------------------------------
    private function entero($dato, $nombre)
    {
        $numero = filter_var($dato, FILTER_VALIDATE_INT);

        if ($numero === false || is_bool($dato)) {
            throw new InvalidArgumentException('falta un entero válido en ' . $nombre);
        }

        return $numero;
    }
}
