varying vec4 vertTexCoord;
uniform sampler2D tex;
uniform vec2 mousedrag;
uniform float zoom;

void main()
{
	vec2 st = vertTexCoord.st;
	st.y = 1.-st.y;
	float u = (st.x * (1.-zoom)) + (0.5 * st.x * zoom);
	float v = (st.y * (1.-zoom)) + (0.5 * st.y * zoom);
	vec2 dest = vec2(u-((mousedrag.x/2.)*zoom),v-((mousedrag.y/2.)*zoom));
		
	gl_FragColor = vec4(texture2D(tex, dest).rgb,1.);
	
}
