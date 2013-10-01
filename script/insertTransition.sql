insert into transition(id, locfrom , direction , locto , isborder , istriggered)
values(
'well_down', 
(select id from location where title = 'У покосившегося сруба.'), 
'trigger',
(select id from location where title = 'Дно колодца'),
0,
1)
