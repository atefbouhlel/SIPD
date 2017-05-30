Dim app 'As Excel.Application
dim wb 'As Workbook
Dim w 'As Worksheet
Dim r, wr, wc 'As Integer
Dim t, wt, v 'As String

set app = CreateObject("Excel.Application")

set wb = app.Workbooks.Open( "d:\96.xls" )
app.Visible = True

r = 1
wt = ""
t = app.Worksheets("96.xml").Cells(r, 1).Value
While t <> ""
    ' create new table-tab if necessary
    If wt <> t Then
        wt = t
		app.Cells.Select
		app.Cells.EntireColumn.AutoFit
        Set w = app.Worksheets.Add
        w.Name = wt
        v = ""
        wr = 1
        w.Cells(wr, 1).Value = "IdGlobal"
        w.Cells(wr, 2).Value = "Author"
        w.Cells(wr, 3).Value = "TSSPT"
        w.Cells(wr, 4).Value = "TSSanteos"
    End If
    ' fill columns
    If v <> app.Worksheets("96.xml").Cells(r, 2).Value Then
        v = app.Worksheets("96.xml").Cells(r, 2).Value
        wr = wr + 1
        wc = 5
    End If
    w.Cells(wr, 1).Value = v
    w.Cells(wr, 2).Value = app.Worksheets("96.xml").Cells(r, 3).Value
    w.Cells(wr, 3).Value = app.Worksheets("96.xml").Cells(r, 4).Value
    w.Cells(wr, 4).Value = app.Worksheets("96.xml").Cells(r, 5).Value
    w.Cells(wr, wc).Value = app.Worksheets("96.xml").Cells(r, 6).Value
    wc = wc + 1
    ' read next row from source table
    r = r + 1
    t = app.Worksheets("96.xml").Cells(r, 1).Value
Wend

' save all and give user control
wb.Save
app.UserControl = True

'app.Quit
'Set app = Nothing
