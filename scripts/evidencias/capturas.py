"""Toma capturas reales del sistema en ejecución (formulario y listado de tickets)."""
import os, sys
from playwright.sync_api import sync_playwright

FRONT = os.environ.get("FRONT_URL", "http://localhost:4173")
EMAIL = os.environ["APP_ADMIN_EMAIL"]
PASS = os.environ["APP_ADMIN_PASSWORD"]
OUT = sys.argv[1]
os.makedirs(OUT, exist_ok=True)

with sync_playwright() as p:
    b = p.chromium.launch()
    page = b.new_page(viewport={"width": 1440, "height": 860})
    page.goto(f"{FRONT}/login")
    page.fill("#login-email", EMAIL)
    page.fill("#login-password", PASS)
    page.click("button[type=submit]")
    page.wait_for_url("**/admin**", timeout=30000)

    page.goto(f"{FRONT}/admin/incidentes")
    page.wait_for_selector("table.admin-table", timeout=30000)
    page.wait_for_timeout(800)
    page.screenshot(path=f"{OUT}/02_listado_tickets.png")

    page.click("text=+ Reportar incidente")
    page.wait_for_selector(".modal-overlay")
    page.fill("input[placeholder^='Ej.: Error 500']", "Error 500 al registrar un cliente nuevo")
    page.fill("textarea", "Al guardar un cliente nuevo desde el panel, la API responde 500 y el registro no se completa.")
    selects = page.locator(".modal-overlay select")
    selects.nth(1).select_option("Alto")
    selects.nth(2).select_option("Media")
    page.wait_for_timeout(400)
    page.screenshot(path=f"{OUT}/01_formulario_reporte.png")
    page.click("text=Enviar reporte")
    page.wait_for_selector(".modal-overlay", state="detached", timeout=30000)
    page.wait_for_timeout(800)
    page.screenshot(path=f"{OUT}/03_listado_con_nuevo_ticket.png")

    page.click("table.admin-table tbody tr:first-child .btn-edit")
    page.wait_for_selector(".modal-overlay")
    page.wait_for_timeout(500)
    page.screenshot(path=f"{OUT}/04_atencion_del_ticket.png")
    b.close()
print("capturas listas")
