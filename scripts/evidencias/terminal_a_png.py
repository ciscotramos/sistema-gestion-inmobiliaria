"""Convierte la salida de texto REAL de un comando en una imagen estilo terminal."""
import sys
from PIL import Image, ImageDraw, ImageFont

entrada, salida, titulo = sys.argv[1], sys.argv[2], sys.argv[3]
lineas = open(entrada, encoding="utf-8", errors="replace").read().rstrip("\n").split("\n")
fuente = ImageFont.truetype("/usr/share/fonts/truetype/dejavu/DejaVuSansMono.ttf", 15)
ancho = max(len(l) for l in lineas + [titulo]) * 9 + 40
alto = (len(lineas) + 3) * 21 + 20
img = Image.new("RGB", (ancho, alto), "#0f172a")
d = ImageDraw.Draw(img)
d.rectangle([0, 0, ancho, 30], fill="#1e293b")
d.text((14, 6), titulo, font=fuente, fill="#94a3b8")
y = 44
for l in lineas:
    d.text((14, y), l, font=fuente, fill="#e2e8f0")
    y += 21
img.save(salida)
