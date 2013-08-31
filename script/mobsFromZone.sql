select distinct m.* from mob m
join habitation h on h.mob = m.id
join location l on h.location = l.id
join zone z on l.zone = z.id
where z.name = 'Малиновый сад'
