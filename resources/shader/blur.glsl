#ifdef GL_ES
precision mediump float;
precision mediump int;
#endif

#define PROCESSING_TEXTURE_SHADER

varying vec4 vertTexCoord;

uniform vec2 resolution;
uniform sampler2D texture;
uniform vec2 texOffset;

void main(void)
{
	   float stepSize = 1. / 1080.;
	   float alpha;
       // get color of pixels:

	  vec2 resi = new vec2(3840,1080);

	  vec2 texturePos = gl_FragCoord.xy/resi.xy;

	  vec2 tc0 = texturePos.st + vec2(-texOffset.s, -texOffset.t);
	  vec2 tc1 = texturePos.st + vec2(         0.0, -texOffset.t);
	  vec2 tc2 = texturePos.st + vec2(+texOffset.s, -texOffset.t);
	  vec2 tc3 = texturePos.st + vec2(-texOffset.s,          0.0);
	  vec2 tc4 = texturePos.st + vec2(         0.0,          0.0);
	  vec2 tc5 = texturePos.st + vec2(+texOffset.s,          0.0);
	  vec2 tc6 = texturePos.st + vec2(-texOffset.s, +texOffset.t);
	  vec2 tc7 = texturePos.st + vec2(         0.0, +texOffset.t);
	  vec2 tc8 = texturePos.st + vec2(+texOffset.s, +texOffset.t);

	  vec4 col0 = texture2D(texture, tc0);
	  vec4 col1 = texture2D(texture, tc1);
	  vec4 col2 = texture2D(texture, tc2);
	  vec4 col3 = texture2D(texture, tc3);
	  vec4 col4 = texture2D(texture, tc4);
	  vec4 col5 = texture2D(texture, tc5);
	  vec4 col6 = texture2D(texture, tc6);
	  vec4 col7 = texture2D(texture, tc7);
	  vec4 col8 = texture2D(texture, tc8);

	  vec4 sum = 8.0 * col4 - (col0 + col1 + col2 + col3 + col5 + col6 + col7 + col8);
	  gl_FragColor = vec4(sum.rgb, 1.0);
}
