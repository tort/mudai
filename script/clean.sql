delete from disposition where location in (select ul.id from location l join zone z on l.zone = z.id join location ul on l.title = ul.title and ul.desc = l.desc where z.name = 'Первая родовая' and ul.zone is null
order by l.title);

delete from habitation where location in (select ul.id from location l join zone z on l.zone = z.id join location ul on l.title = ul.title and ul.desc = l.desc where z.name = 'Первая родовая' and ul.zone is null
order by l.title);

delete from transition where locfrom in (select ul.id from location l join zone z on l.zone = z.id join location ul on l.title = ul.title and ul.desc = l.desc where z.name = 'Первая родовая' and ul.zone is null
order by l.title) or locto in (select ul.id from location l join zone z on l.zone = z.id join location ul on l.title = ul.title and ul.desc = l.desc where z.name = 'Первая родовая' and ul.zone is null
order by l.title); 

delete from location where id in (select ul.id from location l join zone z on l.zone = z.id join location ul on l.title = ul.title and ul.desc = l.desc where z.name = 'Первая родовая' and ul.zone is null
order by l.title);
