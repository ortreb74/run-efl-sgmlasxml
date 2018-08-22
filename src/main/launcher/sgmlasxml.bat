java -cp "c:/app/sgmlAsXml/lib/*" program.Sgml2Xml "%1"

IF %ERRORLEVEL% NEQ 0 GOTO label
GOTO fin

:label
pause 

:fin
pause