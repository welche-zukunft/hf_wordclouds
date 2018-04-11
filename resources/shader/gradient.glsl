varying vec4 vertTexCoord;

uniform vec3 color1;
uniform vec3 color2;

void main()
{
	vec2 st = vertTexCoord.st;
	st.x = 1.-st.x;
	vec3 finalcol = mix(color1,color2,st.x);
	gl_FragColor = vec4(finalcol,1.);

}
