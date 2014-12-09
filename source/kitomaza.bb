Graphics 800,600 
SetBuffer(BackBuffer())

Include "LIB_MULTIFUNC.bb"
 

;***************************************************************************************
; GAME GLOBALS
;***************************************************************************************

; Graphics
Global sprites = LoadAnimImage("sprites.png", 40,40,0,20)
Global missile_image = LoadImage("missile.png")
Global powerups = LoadAnimImage("pups.png", 20,20,0,4)
Global shield = LoadImage("shield.png")

; Audio
Global one_up = LoadSound("1up.ogg")
Global ak = LoadSound("ka.ogg")
Global cpup = LoadSound("power_up.ogg")
Global launch = LoadSound("launch.ogg")
Global boom = LoadSound("boom.ogg")
Global mood = LoadSound("title.ogg")
LoopSound(mood)
Global mood_channel
mood_channel = PlaySound(mood)
Global effects_channel
Global mute% = 0

; General Game Architecture
Global lives% 
Global level% 
Global score% 
Global high_score% = load_high_score()
Global max_missiles% = 3		; Max number of missiles player's allowed to have per ship on the screen at once
Global thousands% = 1			; Count to keep track of how often you get a 1UP
Global diagnostic_mode%
Global last_bonus%
Global bonus_layout% 
Global bonus_kills%
Global bonus%
Global new_record%

; Power-Up Stuff
Global power_up% = 0
Global shield_life%
Global rapid_fire_life%

; Alien Stuff That Adjusts By Level
Global x_incr# = 3				; How much the aliens move left/right each turn
Global update_rate% = 155		; How often the aliens update (move left/right/down)
Global bomb_rate% = 300			; How often the aliens drop a new bomb
Dim destroyed_aliens%(60)		; The ID's of all currently destroyed aliens
Global destroyed% = 0			; The number of aliens currently destroyed
Global total_aliens% = 60		; The number of aliens on this board
Global last_ufo%				; Time that we deployed last UFO
Global ufo_freq% = 7000			; The frequency that we send a new ufo out
Global ufo_update_rate% = 100	; How often the UFO updates (move left/right)
Global ufo_on_screen%			; Boolean value for whether there's a UFO on screen right now or not

; HUD
Global hud_red% = 192 : Global hud_green% = 0 : Global hud_blue% = 0
Global hud_rgb_incr% = -1

; Fonts
Global default_font = LoadFont("Arial", 20,False,False,False)
Global score_font = LoadFont("Arial", 36,True,False,False)
Global high_score_font = LoadFont("Arial", 14,True,False,False)


;***************************************************************************************
; TYPES
;***************************************************************************************
Type Stars
	Field class%
	Field px1#
	Field py1#
	Field px2#
	Field py2#
	Field px3#
	Field py3#
	Field px4#
	Field py4#
End Type 

Type Aliens
	Field id%
	Field image%
	Field x#
	Field y#
	Field draw%
	Field power_up%
End Type 

Type Ufos
	Field image%
	Field last_frame%
	Field x#
	Field y#
	Field born%
	Field life_span%
	Field bomb_freq%
	Field last_bomb%
	Field locked_on%
	Field draw%
End Type 

Type Ships
	Field x#
	Field y#
End Type
Global ship.Ships

Type Missiles
	Field x#
	Field y#
	Field r%
	Field b%
	Field g%
	Field y_incr#
	Field owner$
	Field draw%
End Type 

Type PowerUps
	Field x#
	Field y#
	Field id%
	Field draw%
End Type 


; GAME STARTS
main_loop()
End 


;***************************************************************************************
; FUNCTIONS 
;***************************************************************************************

Function main_loop()

	FlushKeys
	
	ChannelVolume(mood_channel, 1.0)
	
	; Title Screen
	title_screen()
	
	; Play Game
	init_vars()
	If mute% = 0
		ResumeChannel(mood_channel)
		ChannelVolume(mood_channel, .4)
	EndIf 
	
	play()	
	
End Function 


Function load_high_score()

	HSCORES = ReadFile("high_score.dat")
	If HSCORES <> 0
		high_score% = ReadInt(HSCORES)
		CloseFile(HSCORES)
	Else 
		CloseFile(HSCORES)
		HSCORES =WriteFile("high_score.dat")
		WriteInt(HSCORES, "100000")
		CloseFile(HSCORES)
		high_score% = 100000
	EndIf 
	Return high_score%

End Function


Function title_screen()

	FlushKeys
	Cls
	
	; Init Stuff
	create_starfield()
	title_font = LoadFont("Arial", 18,True,False,False)	
	SetFont(title_font)
	title = LoadImage("title.png")
	
	now_showing% = 1
	alienl% = 0 : alienr% = 2
	descrip% = 1
	last_anim% = MilliSecs()
	last_descrip% = MilliSecs()
	loop_timer% = MilliSecs()
	
	title_loop% = 1
	While title_loop% = 1
		Cls
		draw_starfield()
		
		If now_showing% = 1 And MilliSecs() - loop_timer% >= 10000
			now_showing% = 2
			descrip% = 1
			loop_timer% = MilliSecs()
			last_descrip% = MilliSecs()
		ElseIf now_showing% = 2 And MilliSecs() - loop_timer% >= 18000
			now_showing% = 1
			loop_timer% = MilliSecs()
		EndIf 
		
		If now_showing% = 1
		
			DrawImage title, 168,160
			If MilliSecs() - last_anim% >= 300
				
				If alienl% = 0
					alienl% = 1 : alienr% = 3
				Else 
					alienl% = 0 : alienr% = 2
				EndIf
				last_anim% = MilliSecs()
				
			EndIf 
			
			DrawImage sprites, 186,121,alienl%
			DrawImage sprites, 592,121,alienr%
		
		Else
		
			Color 255,255,255
			If descrip% > 0
				DrawImage sprites, 300,140,0
				Text 400,155, "x 125 pts"
			EndIf 
			
			If descrip% > 1
				DrawImage sprites, 300,190,2
				Text 400,205, "x 125 pts"
			EndIf 
			
			If descrip% > 2
				DrawImage sprites, 300,240,8
				Text 400,255, "x 250 pts"
			EndIf 
			
			If descrip% > 3
				DrawImage powerups, 310,290,0
				Text 410,295, "Shield"
			EndIf 
			
			If descrip% > 4
				DrawImage powerups, 310,340,1
				Text 410,345, "Rapid Fire"
			EndIf 
			
			If descrip% > 5
				DrawImage powerups, 310,390,2
				Text 410,395, "1 Up"
			EndIf 
			
			If descrip% > 6
				DrawImage powerups, 310,440,3
				Text 410,445, "Buddy"
			EndIf 
			
			If MilliSecs() - last_descrip% >= 2000
				If descrip% < 7 descrip% = descrip% + 1
				last_descrip% = MilliSecs()
			EndIf 
	
		
		EndIf 
		
		If hud_red% = 192
			hud_rgb_incr% = 1
		ElseIf hud_red% = 255 
			hud_rgb_incr% = -1
		EndIf 
		
		hud_red% = hud_red% + hud_rgb_incr%
		hud_green% = hud_green% + hud_rgb_incr%
		hud_blue% = hud_blue% + hud_rgb_incr%
	
		Color hud_red%, hud_green%, hud_blue%
		;Print CurrentDir$()
		Text 300,550, "Copyright 2007, Caretcake."
		Text 210,570, "http://dave.caretcake.com | http://www.caretcake.com"
		
		
		key% = GetKey()
		If key% = 27
			End
		ElseIf key% = 109
			If mute% = 0
				mute% = 1
				PauseChannel(mood_channel)
			Else 
				mute% = 0
				ResumeChannel(mood_channel)
			EndIf 
		ElseIf key% > 0
			title_loop% = 0
		EndIf 
		
		If mute% = 1 Text 375,5, "MUTED"
		
		Flip
		
	Wend
	SetFont(default_font)
	FreeImage(title)
	
End Function 
	

Function init_vars()

	lives% = 3
	level% = 1
	score% = 0
	update_rate% = 155
	x_incr# = level% + 2
	bomb_rate% = 300
	Dim destroyed_aliens%(60)
	destroyed% = 0
	total_aliens% = 60
	ufo_on_screen% = 0
	thousands% = 1
	last_bonus% = 0
	bonus_layout% = 1
	new_record% = 0
	
End Function


Function play()

	; Init 
	ship.Ships = reset_sprites()
	For missile.Missiles = Each Missiles
		Delete missile
	Next
	destroyed% = 0 
	Dim destroyed_aliens(60)
	
	For pup.PowerUps = Each PowerUps
		Delete pup
	Next 

	last_update% = MilliSecs()		; Last time we moved the aliens
	last_bomb% = MilliSecs()		; Last time we dropped a bomb
	last_ufo% = MilliSecs()			; Last time we deployed a UFO
	last_ufo_update% = MilliSecs()	; Last time we moved the UFO
	ufo_on_screen% = 0
	
	create_starfield()
	
	; Bonus Level?
	If last_bonus% + 4 = level% 
		bonus()
		x_incr# = x_incr# + 6
		update_rate% = update_rate% - 50
		last_bonus% = last_bonus% + 4
		level% = level% - 1
	Else 
		bonus% = 0
	EndIf  
	
	; Display Everything with the Level Name
	If level% = 1
		If mute% = 0
			; kito = LoadSound("kitomaza.ogg")
			; effects_channel = PlaySound(kito)
		EndIf 
	EndIf 
	Cls 
	draw_starfield()
	draw_aliens()
	draw_ship(ship.Ships)
	draw_hud()
	Color 255,255,255
	If bonus% = 1 
		Text 370,350, "Bonus Level"
	Else
		Text 375,350, "Level " + level%
	EndIf
	 
	If level% = 1
		Color 255,255,255
		kito_font = LoadFont("Arial", 14,False,True,False)
		SetFont(kito_font)
		Text 305,400, "Boku wa Kitomaza desu.  Surrender Now!"
	EndIf 
	Flip
	If level% = 1
		Delay 4500
	Else 
		Delay 2500
	EndIf 
	If level% = 1 FreeSound(kito)
	FlushKeys 
	
	; Start Round
	play_loop% = 1
	While play_loop% = 1

		Cls 
		
		; Draw Starfield
		draw_starfield()
	
		; Update and Draw Aliens
		If MilliSecs() - last_update% >= update_rate%
			update_aliens()
			last_update% = MilliSecs()
		EndIf 
		draw_aliens()
	
		; Drop Bombs
		If bonus% = 0
			If MilliSecs() - last_bomb% >= bomb_rate%
				fire_missile("c")
				last_bomb% = MilliSecs()
			EndIf
		EndIf  
		
		; Deploy UFO if Necessary
		If ufo_on_screen% = 0 And bonus% = 0
			If MilliSecs() - last_ufo% > ufo_freq%
				ufo.Ufos = deploy_ufo()
				ufo_on_screen% = 1
				last_ufo% = MilliSecs()
			EndIf
		EndIf 
		
		; Update and Draw UFO if Necessary
		If ufo_on_screen% = 1
			update_ufo(ufo.Ufos)
			draw_ufo(ufo.Ufos)
		EndIf 
	
		; Fire, Update and Draw Missiles
		If KeyHit(29) Or KeyHit(57) Or KeyHit(157) fire_missile("h")
		
		update_missiles() : draw_missiles()
		
		
		; Update and Draw Ship
		If KeyDown(203)
			If ship\x# > 0 ship\x# = ship\x# - 5
		EndIf 
	
		If KeyDown(205)
			If ship\x# < 760 ship\x# = ship\x# + 5
		EndIf 	

		; Power Up's
		draw_pups()
		If power_up% = 1 draw_shield(ship.Ships)
		
		draw_ship(ship.Ships)
		
		If diagnostic_mode% = 1
			Color 255,255,255
			Text 0,0, "UPDATE RATE: " + update_rate%
			Text 295,0, "ALIEN X_INCR: " + x_incr#
			Text 650,0, "BOMB RATE: " + bomb_rate%
		EndIf 
		
		; Quit
		If KeyHit(1) 
			play_loop% = 0
			game_over()
		EndIf 
		
		; Pause
		If KeyHit(25)
			FlushKeys
			pause_loop% = 1
			While pause_loop% = 1
				draw_aliens()
				draw_ship(ship.Ships)
				draw_hud()
				Color 0,0,0
				Rect 370,290,81,25,1
				Color 255,255,255
				Text 375,295, "PAUSED!"
				
				Flip 
				
				key% = GetKey()
				If key% = 112
					pause_loop% = 0
					FlushKeys 
				EndIf 
			Wend
		EndIf 
		
		; Mute
		If KeyHit(50)
			If mute% = 0
				mute% = 1
				PauseChannel(mood_channel)
			Else 
				mute% = 0
				ResumeChannel(mood_channel)
			EndIf 
		EndIf 
		If mute% = 1 Color 100,100,100 : Text 375,5, "MUTED"						
				
		; Diagnostic Mode
		If KeyDown(29) And KeyDown(32)
		
			FlushKeys
			If diagnostic_mode% = 0
				diagnostic_mode% = 1
			Else
				diagnostic_mode% = 1
			EndIf 
			
		EndIf 
		
		; HUD
		draw_hud()
	
		Flip
	
	Wend 
End Function 

End 


Function reset_sprites.Ships()

	; Delete all old alien types
	For alien.Aliens = Each Aliens
		Delete alien
	Next 
	
	; Set up vars for power-ups
	SeedRnd(MilliSecs())
	pup1% = Rnd(1,2) : pup2% = Rnd(1,2)
	pup3% = Rnd(1,2) : pup4% = Rnd(1,2)
	pups% = pup1% + pup2% + pup3% + pup4%
	
	; Re-create and reposition all aliens
	alien_image% = 2 : next_# = 0 : id% = 1
	For row% = 1 To 5
	
		next_x# = 130
		next_y# = next_y# + 45
		If alien_image% = 2
			alien_image% = 0
		Else 
			alien_image% = 2
		EndIf 
				
		For column% = 1 To 12
			alien.Aliens = New Aliens
			alien\id% = id%
			alien\image% = alien_image%
			alien\x# = next_x#
			alien\y# = next_y#
			alien\draw% = 2
			
			; Power-up
			If pups% > 0
				add_pup% = Rnd(1,7)
				If add_pup% = 7
					added% = 0
					Repeat
						add_pup% = Rnd(1,4)
						 Select add_pup%
							Case 1
								If pup1% > 0 alien\power_up% = 1 : pup1% = pup1% - 1 : added% = 1
							Case 2 
								If pup2% > 0 alien\power_up% = 2 : pup2% = pup2% - 1 : added% = 1
							Case 3
								If pup3% > 0 alien\power_up% = 3 : pup3% = pup3% - 1 : added% = 1	
							Case 4
								If pup4% > 0 alien\power_up% = 4 : pup4% = pup4% - 1 : added% = 1		
						End Select
					Until added% = 1
					pups% = pups% - 1
				EndIf 
			EndIf 				  
		
			alien_image% = alien_image% + 2
			If alien_image% = 4 Then alien_image% = 1
			If alien_image% = 5 Then alien_image% = 0
			next_x# = next_x# + 45
			
			id% = id% + 1
		Next
	Next 
	
	; Delete all old ships
	For ship.Ships = Each Ships
		Delete ship
	Next 
	
	; Re-create and reposition ship
	ship.Ships = New Ships
	ship\x# = 380
	ship\y# = 500
	Return ship
	
End Function 



Function create_starfield()

	For star.Stars = Each Stars
		Delete star
	Next 
	
	SeedRnd(MilliSecs())
	For i% = 1 To 50
		class% = Rnd(1,3)
		star.Stars = New Stars
		
		Select class%
			Case 1 ; Biggest
				star\class% = class%
				star\px1# = Rnd(0,800)
				star\py1# = Rnd(0,500)
				star\px2# = star\px1# + 1
				star\py2# = star\py1#
				star\px3# = star\px1#
				star\py3# = star\py1# + 1
				star\px4# = star\px3# + 1
				star\py4# = star\py4# + 1
				
			Case 2 ; Medium
				star\class% = class%
				star\px1# = Rnd(0,800)
				star\py1# = Rnd(0,500)
				star\px2# = star\px1# + 1
				star\py2# = star\py1#
				
			Case 3 ; Smallest
				star\class% = class%
				star\px1# = Rnd(0,800)
				star\py1# = Rnd(0,500)
				
		End Select 
	Next 
			

End Function


Function draw_starfield()

	For star.Stars = Each Stars
		Select star\class%
			Case 1 ; Biggest 
				star\py1# = star\py1# + 3
				star\py2# = star\py2# + 3
				star\py3# = star\py3# + 3
				star\py4# = star\py4# + 3 
				Color 255,255,255
				Plot star\px1#, star\py1#
				Plot star\px2#, star\py2#
				Plot star\px3#, star\py3#
				Plot star\px4#, star\py4#
				
				If star\py1# > 500
					star\py1# = -2
					star\py2# = -2
					star\py3# = -1
					star\py4# = -1
				EndIf 
				
			Case 2 ; Medium
				star\py1# = star\py1# + 1.5
				star\py2# = star\py2# + 1.5
				Color 155,155,155
				Plot star\px1#, star\py1#
				Plot star\px2#, star\py2#
				
				If star\py1# > 500
					star\py1# = -2
					star\py2# = -2
				EndIf 
				
			Case 3 ; Smallest
				star\py1# = star\py1# + .75
				Color 55,55,55
				Plot star\px1#, star\py1#
				
				If star\py1# > 500
					star\py1# = -2
				EndIf 
				
		End Select 
	Next 
				 
End Function 


Function update_aliens()

	drop% = 0

	; Move all x_incr
	For alien.Aliens = Each Aliens 
		If alien\draw% > 0
			next_x# = alien\x# + x_incr#
			If next_x# < 0 Or next_x# > 760 drop% = 1
		EndIf 
	Next 
	
	; Drop rows if drop% = 1
	If drop% = 1
		If x_incr# > 0
			x_incr# = x_incr# + .25
		Else 
			x_incr# = x_incr# - .25
		EndIf 
		x_incr# = x_incr# * -1
			
		For alien.Aliens = Each Aliens
			If alien\draw% > 0
				If bonus% = 1 And alien\y# >= 495
					alien\draw% = 0
					destroyed% = destroyed% + 1
					If destroyed% = total_aliens% win_level()
				ElseIf alien\y# < 495 
					alien\y# = alien\y# + 45
				EndIf 
			EndIf 
		Next 
		
		update_rate% = update_rate% - 2
	EndIf 
	
	; Shift left/right and update frame
	For alien.Aliens = Each Aliens
		If alien\draw% > 0
			alien\x# = alien\x# + x_incr#
			Select alien\image%
				Case 0 : alien\image% = 1
				Case 1 : alien\image% = 0
				Case 2 : alien\image% = 3
				Case 3 : alien\image% = 2
			End Select 
		EndIf 
	Next 
	
End Function 


Function draw_aliens()
			
	For alien.Aliens = Each Aliens
		If alien\draw% = 0
			Delete alien
		Else 
			DrawImage sprites, alien\x%, alien\y%, alien\image%
			If alien\image% > 3 alien\draw% = alien\draw% - 1
		EndIf 
	Next
	
End Function  


Function deploy_ufo.Ufos()

	ufo.Ufos = New Ufos
	ufo\image% = 8
	ufo\last_frame% = MilliSecs()
	ufo\x# = -40
	ufo\y# = 5
	ufo\born% = MilliSecs()
	ufo\life_span% = 10000
	ufo\bomb_freq% = bomb_rate% * 2
	ufo\locked_on%  = 0
	ufo\draw% = 2
	Return ufo.Ufos
	
End Function 	

Function update_ufo(ufo.Ufos)

	If MilliSecs() - ufo\born% >= ufo\life_span% Then ufo\locked_on% = 0
	If ufo\locked_on% = 0
		If ufo\x# = ship\x#
			ufo\locked_on% =  1
		Else
			ufo\x# = 	ufo\x# + 5
		EndIf 
	Else 
		ufo\x# = ship\x#
		If MilliSecs() - ufo\last_bomb% > ufo\bomb_freq%
		
			; Drop Bomb		
			missile.Missiles = New Missiles
			missile\x# = ufo\x# + 19
			missile\y# = ufo\y# + 40
			missile\r% = 255
			missile\g% = 64 
			missile\b% = 64
			missile\y_incr# = 5.5
			missile\owner$ = "c"
			missile\draw% = 1
			
			ufo\last_bomb% = MilliSecs()
			
		EndIf 
		
	EndIf 
	
	If ufo\x# > 805 ufo\draw% = 0
	
End Function 


Function draw_ufo(ufo.Ufos)

	If ufo\draw% = 0
		Delete ufo
		ufo_on_screen% = 0
		last_ufo% = MilliSecs()
	Else 

		If MilliSecs() - ufo\last_frame% >= 300
			Select ufo\image%
				Case 8 : ufo\image% = 9
				Case 9 : ufo\image% = 10
				Case 10 : ufo\image% = 11
				Case 11 : ufo\image% = 8
			End Select
			ufo\last_frame% = MilliSecs()
		EndIf  
		If ufo\image% > 11 ufo\draw% = ufo\draw% - 1
		DrawImage sprites, ufo\x#, ufo\y#, ufo\image%
	
	EndIf 
	
End Function 		
			
			
Function draw_ship(ship.Ships)

	If bonus% = 0
		For alien.Aliens = Each Aliens
			If alien\image% <= 3 And alien\y# >= 420
				; Normal
				If ImagesCollide(sprites, ship\x#, ship\y#, 16, sprites, alien\x#, alien\y#, alien\image%)
					If mute% = 0 effects_channel = PlaySound(ak)
					score% = score% + 125
					destroyed_aliens%(alien\id%)  = 1
					If alien\image% <= 3 destroyed% = destroyed% + 1
					alien\image% = alien\image% + 4
					If power_up% = 4
						power_up% = 0
					ElseIf power_up% <> 1
						lose_life()
					EndIf 
					If destroyed% = total_aliens%
						win_level()
					EndIf 
				EndIf 
				; Buddy
				If power_up% = 4
					If ImagesCollide(sprites, ship\x# + 40, ship\y#, 16, sprites, alien\x#, alien\y#, alien\image%)
						If mute% = 0 effects_channel = PlaySound(ak)
						score% = score% + 125
						destroyed_aliens%(alien\id%)  = 1
						If alien\image% <= 3 destroyed% = destroyed% + 1
						alien\image% = alien\image% + 4
						power_up% = 0
						If destroyed% = total_aliens%
							win_level()
						EndIf 
					EndIf 
				EndIf 
			EndIf 
		Next 
	EndIf 
	
	For pup.PowerUps = Each PowerUps
		If ImagesCollide(sprites, ship\x#, ship\y#, 16, shield, pup\x#, pup\y#, 0)
			If mute% = 0 effects_channel = PlaySound(cpup)
			pup\draw% = 0
			power_up% = pup\id%
			
			Select pup\id%
				Case 1 : shield_life% = 10
				Case 2 : rapid_fire_life% = 50
				Case 3 : lives% = lives% + 1 : If mute% = 0 effects_channel = PlaySound(one_up)
			End Select 
			
		EndIf 
	Next 
	
	DrawImage sprites, ship\x#, ship\y#, 16
	If power_up% = 4 DrawImage sprites, ship\x# + 40, ship\y#, 16
	
End Function 
				

Function fire_missile(owner$)

	If owner$ = "h"
	
		For missile.Missiles = Each Missiles
			If missile\owner$ = "h" on_screen% = on_screen% + 1
		Next 
		
		If (power_up% <> 2 And on_screen% < max_missiles%) Or (power_up% = 2)
			If mute% = 0 effects_channel = PlaySound(launch)
			missile.Missiles = New Missiles
			missile\x# = ship\x# + 19
			missile\y# = 500
			missile\r% = 255
			missile\g% = 255
			missile\b% = 0
			missile\y_incr# = -6
			missile\owner$ = "h"
			missile\draw% = 1
			If power_up% = 2
				If rapid_fire_life% <= 0 power_up% = 0
				rapid_fire_life% = rapid_fire_life% - 1
			EndIf 	
			
			; Buddy
			If power_up% = 4
				missile.Missiles = New Missiles
				missile\x# = ship\x# + 59
				missile\y# = 500
				missile\r% = 255
				missile\g% = 255
				missile\b% = 0
				missile\y_incr# = -6
				missile\owner$ = "h"
				missile\draw% = 1		
			EndIf 	
		EndIf 
	EndIf 	
	
	If owner$ = "c"
	
		If destroyed% < total_aliens%
			SeedRnd(MilliSecs())
			SndSeed% =  Rnd(99999999,9999999999999999)
			SeedRnd(SndSeed%)
			
			id% = 0
			Repeat
				id% = Rnd(1,60)
				If destroyed_aliens%(id%) = 1 id% = 0
			Until id% > 0
			
			For alien.Aliens = Each Aliens
				If alien\id% = id%
					missile.Missiles = New Missiles
					missile\x# = alien\x# + 19
					missile\y# = alien\y# + 40
					missile\r% = 255
					missile\g% = 64
					missile\b% = 64
					missile\y_incr# = 4
					missile\owner$ = "c"
					missile\draw% = 1
				EndIf 
			Next 	
		EndIf 
	
	EndIf 

End Function  


Function update_missiles()

	For missile.Missiles = Each Missiles
		missile\y# = missile\y# + missile\y_incr#
		If missile\y# < 0 Or missile\y# > 540 missile\draw% = 0
		
		If missile\owner$ = "h"
			For alien.Aliens = Each Aliens
				If alien\image% < 4
					If ImagesCollide(sprites, alien\x#, alien\y#, alien\image%, missile_image, missile\x#, missile\y#, 0)
						If mute% = 0 effects_channel = PlaySound(ak)
						score% = score% + 125
						missile\draw% = 0
						If alien\power_up% > 0 release_power_up(alien\power_up%, alien\x#, alien\y#)
						destroyed_aliens%(alien\id%)  = 1
						destroyed% = destroyed% + 1
						If bonus% = 1 bonus_kills% = bonus_kills% + 1
						alien\image% = alien\image% + 4
						If destroyed% = total_aliens%
							win_level()
						EndIf 
					EndIf
				EndIf  
			Next 
			
			If missile\draw% > 0
				For ufo.Ufos = Each Ufos
					If ufo\image% < 12
						If ImagesCollide(sprites, ufo\x#, ufo\y#, ufo\image%, missile_image, missile\x#, missile\y#, 0)
							If mute% = 0 effects_channel = PlaySound(ak)
							score% = score% + 250
							missile\draw% = 0
							ufo\image% = ufo\image% + 4
						EndIf
					EndIf  
				Next 
			EndIf 
		EndIf 
		
		If missile\owner$ = "c"
		
			If power_up% = 1
				If ImagesCollide(shield, ship\x# - 20, ship\y# - 20, 0, missile_image, missile\x#, missile\y#, 0) 
					missile\draw% = 0
					shield_life% = shield_life% - 1
					If shield_life% <= 0 power_up% = 0
				EndIf 
			
			ElseIf power_up% = 4
				If ImagesCollide(sprites, ship\x#, ship\y#, 16, missile_image, missile\x#, missile\y#, 0) Or ImagesCollide(sprites, ship\x# + 40, ship\y#, 16, missile_image, missile\x#, missile\y#, 0)
					power_up% = 0
					missile\draw% = 0
				EndIf 
			
			ElseIf  missile\draw% > 0
				If ImagesCollide(sprites, ship\x#, ship\y#, 16, missile_image, missile\x#, missile\y#, 0) 
					lose_life()
				EndIf
			EndIf 
			
		EndIf  
		
	Next 
	
End Function 


Function draw_missiles()

	For missile.Missiles = Each Missiles
		If missile\draw% = 0
			Delete missile
		Else 
			Color missile\r%, missile\g%, missile\b%
			Rect missile\x#, missile\y#, 3, 10
		EndIf 
	Next 
	
End Function 	


Function release_power_up(id%, x#, y#)

	pup.PowerUps = New PowerUps
	pup\x# = x#
	pup\y# = y#
	pup\id% = id%
	pup\draw% = 1

End Function


Function draw_pups()

	For pup.PowerUps = Each PowerUps
		pup\y# = pup\y# + 3
		If pup\y# > 540 pup\draw% = 0
		If pup\draw% = 1
			DrawImage powerups, pup\x#, pup\y#, pup\id% - 1
		Else 
			Delete pup
		EndIf 
	Next 
	
End Function 	


Function draw_shield(ship.Ships)

	DrawImage shield, ship\x# - 10, ship\y# - 2

End Function 

	
Function win_level()

	PauseChannel(mood_channel)
	Cls
	draw_starfield()
	DrawImage sprites, ship\x#, ship\y#, 16
	draw_hud()
	
	If bonus% = 1
		Color 255,255,255
		Text 335,200, "Accuracy:  " + bonus_kills% + "/30"
		If bonus_kills% = 30 Text 350,230, "EXCELLENT!"
	EndIf 	
	
	Flip
	Delay 2000
	If bonus% = 1
		bonus% = 0
		Delay 2000
	End If 
	
	; Re-Init Vars	
	If level% < 10
		update_rate% = 155 - (level% * 5)
	Else 
		update_rate% = 110
	EndIf 
	
	If level% < 10
		x_incr# = level% + 2
	Else 
		x_incr# = 12
	EndIf 
	
	If bomb_rate% > 100 bomb_rate% = bomb_rate% - 5
	level% =  level% + 1
	power_up% = 0
	If mute% = 0 ResumeChannel(mood_channel)
	play()	

End Function		


Function draw_hud()

	; Glowing Background and Perimiter
	Color 128,0,0
	Rect -1,541,801,5,1
	
	If hud_red% = 192
		hud_rgb_incr% = 1
	ElseIf hud_red% = 255 
		hud_rgb_incr% = -1
	EndIf 
	
	hud_red% = hud_red% + hud_rgb_incr%
	hud_green% = hud_green% + hud_rgb_incr%
	hud_blue% = hud_blue% + hud_rgb_incr%

	Color hud_red%, hud_green%, hud_blue%
	Rect -1,546,802,59,1
	
	; Lives	
	Color 0,0,0
	DrawImage sprites, 10,550,16
	Text 55,570, "X " + lives%
	
	; Level
	Text 710,570, "LEVEL: " + level%
	
	; High Score
	SetFont(high_score_font)
	If score% > high_score% 
		high_score% = score%
		If new_record% = 0 
			new_high = LoadSound("high_score.ogg")
			effects_channel = PlaySound(new_high)
			new_record% = 1
			Delay 600
			FreeSound(new_high)
		EndIf 
	EndIf 
	
	high_score_string$ = high_score%
	If Len(high_score_string$) < 10
		For i% = 1 To 10 - Len(high_score_string$)
			zeros$ = zeros$ + "0"
		Next
	EndIf 
	Text 350,550, "ICHIBAN  " + zeros$ + high_score%
	
	; Current Score
	SetFont(score_font)
	score_string$ = score%
	If Len(score_string$) < 10
		For i% = 1 To 10 - Len(score_string$)
			zeros$ = zeros$ + "0"
		Next 
	EndIf 
	Text 280,560, zeros$ + score%
	
	SetFont(default_font)
	
	; 1UP Every 15k Points
	If score% > thousands% * 15000
		thousands% = thousands% + 1
		lives% = lives% + 1
		If mute% = 0 effects_channel = PlaySound(one_up)
	EndIf 
	

End Function


Function lose_life()

	; Draw Explosion
	If mute% = 0 effects_channel = PlaySound(boom)
	DrawImage sprites, ship\x#, ship\y#, 17
	draw_aliens()
	draw_hud()
	Flip
	Delay 80
	
	DrawImage sprites, ship\x#, ship\y#, 18
	draw_aliens()
	draw_hud()
	Flip
	Delay 80
	
	DrawImage sprites, ship\x#, ship\y#, 19
	draw_aliens()
	draw_hud()
	Flip
	Delay 50
	Cls 
	
	; Subtract Life, Remove Power-Up's and Test for Game Over	
	lives% = lives% - 1
	If lives% = 0 game_over()
	power_up% = 0
	
	; Set up Screen to Start Again
	; Kill UFO
	For ufo.Ufos = Each Ufos
		Delete ufo
	Next 
	ufo_on_screen% = 0
	
	; Reposition Ship	
	ship\x# = 380
	ship\y# = 500
	DrawImage sprites, ship\x#, ship\y#, 16
	
	; Draw Everything Else
	draw_starfield()
	draw_aliens()
	draw_hud()
	
	For missile.Missiles = Each Missiles
		Delete missile
	Next 
	
	; Get Ready!
	Color 0,0,0
	Rect 360,290,105,25,1
	Color 255,255,255
	Text 365,295, "GET READY!"
		
	Flip
	Delay 1500
	FlushKeys
	
End Function 


Function game_over()

	started% = MilliSecs()
	FlushKeys 
	Repeat 
	
		Cls
		draw_starfield()
		draw_aliens()
		draw_hud()
		Color 255,255,255
		Color 0,0,0
		Rect 360,290,105,25,1
		Color 255,255,255
		Text 365,295, "GAME OVER!"
		Flip 
	
	Until MilliSecs() - started% >= 3000
	FlushKeys

	; Check for new High Score
	If high_score% = score%
		HSCORE = WriteFile("high_score.dat")
		WriteInt(HSCORE, high_score%)
		CloseFile(HSCORE)
	EndIf 	
	
	main_loop()
	
End Function 


Function bonus()

	Select bonus_layout%
		Case 1 : show$ = "1,2,3,4,12,11,10,9,13,14,23,24,28,29,30,31,32,33,37,38,47,48,49,50,51,52,57,58,59,60"
		Case 2 : show$ = "3,4,7,8,11,12,15,16,19,20,23,24,27,28,31,32,35,36,39,40,43,44,47,48,51,52,55,56,59,60"
		Case 3 : show$ = "2,3,6,7,10,11,13,16,17,20,21,24,25,28,29,32,33,36,37,40,41,44,45,48,50,51,54,55,58,59"
	End Select
	
	dim_sum% = MULTI_PARSE(",",show$)
	For alien.Aliens = Each Aliens
		For i% = 1 To dim_sum%
			id% = MULTIFUNC$(i%)
			If alien\id% = id%
				alien\draw% = 0
				destroyed% = destroyed% + 1
				destroyed_aliens%(id%) = 1
			EndIf
		Next
	Next 
	
	bonus% = 1
	bonus_kills% = 0
	
	If bonus_layout% = 1
		bonus_layout% = bonus_layout% + 1
	ElseIf bonus_layout% = 2
		bonus_layout% = 3
	Else 
		bonus_layout% = 1
	EndIf 
	
End Function



	
	