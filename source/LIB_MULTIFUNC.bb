;****************************************************************************************
;
; LIB_MULTIFUNC.bb
; Written On:  04-07-03
; Last Updated On:  04-23-03
;
; Summary:  Because of Blitz's inability to return more than one argument from functions, this workaround 
; library was created.  When you need to pass more than one argument back from a functions, first, stringify
; and delimit the return values:
; 
; Ex.
; two_vars$ = foo()
; Function foo()
;	arg1% = 1
;	arg2% = 2
;	Return arg1% + "|" + arg2%
; End Function
;
; Now, parse two_vars% using the MULTI_PARSE(delimiter$, string$) function in LIB_MULTIFUNC.bb
;
; Ex.
; MULTI_PARSE("|", two_vars$)
; arg1% = MULTIFUNC$(1) ; = 1
; arg2% = MULTIFUNC$(2) ; = 2
;
; The variable type declarations against MULTIFUNC$(index) will automatically adjust the variable to whatever 
; type is needed [%,$,#].
;
;****************************************************************************************

; Declare the global array that will hold all values passed back by MULTI_PARSE()
Dim MULTIFUNC$(1)


; Function that separates strings out based on a particular delimiter
Function MULTI_PARSE%(delimiter$, target$)

	; Determine . . . 
	length% = Len(target$)						; Length of target string
	last_delim% = 1							; Where to start the Mid() grab
	dim_sum% = GET_DIM_SUM(delimiter$, target$)	; Number of delimiters in target string
	Dim MULTIFUNC$(dim_sum%)				; Number of elements in array = Number of delimiters in target
	i% = 1									; Array index, MULTIFUNC$(i%)
	
	; Go through target string, character by character, looking for delimiter$, every time it's found, Mid() that 
	; group of characters out and store in MULTIFUNC$(i%)
	For char_at% = 1 To length%
	
		char$ = Mid(target$, char_at%, 1)
		If char$ = delimiter$
			; Values in beginning and middle of target string
			distance% = char_at% - last_delim%
			MULTIFUNC$(i%) = Mid(target$, last_delim%, distance%)
			last_delim% = char_at% + 1
			i% = i% + 1
			
		ElseIf char_at% = length%
			; Very last value in target string
			distance% = char_at% - last_delim% + 1
			MULTIFUNC$(i%) = Mid(target$, last_delim%, distance%)
		End If 
	Next
	
	; Return number of elements in MULTIFUNC$()
	Return dim_sum%
	
End Function
	


; Function to determine number of delimiters in target string.  This determines re-size of MULTIFUNC$() array.
Function GET_DIM_SUM%(delimiter$, target$)

	length% = Len(target$)
	last_delim% = 1
	i% = 1

	For char_at% = 1 To length%
	
		char$ = Mid(target$, char_at%, 1)
		If char$ = delimiter$
			i% = i% + 1
		EndIf 
		
	Next
	
	Return i%

End Function