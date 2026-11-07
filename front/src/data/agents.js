export const agents = [
  {
    id: 1,
    nombre: "Juan Carlos Pérez",
    cargo: "Asesor Inmobiliario",
    telefono: "+51 987 654 321",
    email: "juan.perez@inmobiliariadelsur.com",
    foto: "https://images.unsplash.com/photo-1560250097-0b93528c311a?w=200&q=80",
  },
  {
    id: 2,
    nombre: "María Fernanda López",
    cargo: "Agente Senior",
    telefono: "+51 987 123 456",
    email: "maria.lopez@inmobiliariadelsur.com",
    foto: "https://images.unsplash.com/photo-1573496359142-b8d87734a5a2?w=200&q=80",
  },
  {
    id: 3,
    nombre: "Carlos Enrique Torres",
    cargo: "Asesor Comercial",
    telefono: "+51 987 789 012",
    email: "carlos.torres@inmobiliariadelsur.com",
    foto: "https://images.unsplash.com/photo-1472099645785-5658abf4ff4e?w=200&q=80",
  },
];

export const getAgentById = (id) => agents.find((a) => a.id === id);
