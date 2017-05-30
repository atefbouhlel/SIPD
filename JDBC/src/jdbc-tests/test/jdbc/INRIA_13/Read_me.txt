====================================================================
Description : Performance test
====================================================================
MESURE I : Insert form (without comment)
-
| INSERT INTO EVENT (1x)
| INSERT INTO INFO (10x)
| COMMIT;
-
MESURE II - re-compose chronological view (3 options) :
1) 1 query (retrieve names of forms, event dates for each event of the form, user name for the event)
-
| SELECT form.nom, event.date, user.nom FROM form, user, event WHERE event.idUser=user.IdGlobal and event.idform=form.IdGlobal
- 
2) 1 query per form
-
| SELECT nom, IdGlobal FROM form
| SELECT event.date, user.nom FROM user, event WHERE event.idUser=user.IdGlobal and event.idform=?
- (second query executed 3 times : must qualify 0, 1, N events)
3) 1 query per form (involves form.idglobal to use the climbing index)
-
| SELECT nom, IdGlobal FROM form
| SELECT event.date, user.nom FROM user, event, form WHERE event.idUser=user.IdGlobal and form.IdGlobal=e.IdForm and form.idglobal=?
-(second query executed 3 times : must qualify 0, 1, N events)

MESURE III - open a form from the chronological view (5 options) :
1) retrieve header (user, date), then infos
-
| SELECT user.nom, event.date FROM user, event WHERE user.IdGlobal=event.IdUser and event.idform=form.IdGlobal and event.IdGlobal=X;
| SELECT * FROM info, comment, concept WHERE info.IdConcept=concept.IdGlobal and info.IdComment=comment.IdGlobal and info.IdEvent=X;
-
2) idem, by retrieve infos from event (involves event.idglobal to use the climbing index)
-
| SELECT user.nom, event.date FROM user, event WHERE user.IdGlobal=event.IdUser and event.idform=form.IdGlobal and event.IdGlobal=X;
| SELECT * FROM event, info, comment, concept WHERE info.Idevent=event.IdGlobal anb info.IdConcept=concept.IdGlobal and info.IdComment=comment.IdGlobal and event.IdGlobal=X;
-
3) retrieve everything from event.idglobal
-
| SELECT * FROM user, event, info, comment, concept WHERE user.idGlobal=event.Iduser and info.Idevnet=event.IdGlobal anb info.IdConcept=concept.IdGlobal and info.IdComment=comment.IdGlobal and event.IdGlobal=X;
-
4) retrieve everything from event.date and form.idglobal
-
| SELECT * FROM user, event, info, comment, concept WHERE user.idGlobal=event.Iduser and info.Idevnet=event.IdGlobal anb info.IdConcept=concept.IdGlobal and info.IdComment=comment.IdGlobal and event.IdGlobal=X AND event.date=Y;
-
5) retrieve everything from event.date and form.nom
-
| SELECT * FROM form, user, event, info, comment, concept WHERE form.idglobal=event.idform and user.idGlobal=event.Iduser and info.Idevnet=event.IdGlobal anb info.IdConcept=concept.IdGlobal and info.IdComment=comment.IdGlobal AND event.date=Y AND form.nom=X;
- 


