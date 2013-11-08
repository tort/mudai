select t.id, lf.title, t.direction, lt.title from transition t join location lf on t.locfrom  = lf.id join location lt on t.locto = lt.id
where lt.title = 'В яме'
