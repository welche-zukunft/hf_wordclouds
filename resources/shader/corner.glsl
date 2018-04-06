#ifndef HALF_PI
#define HALF_PI 1.5707963267948966
#endif

#define PROCESSING_TEXTURE_SHADER

uniform vec2 resolution;
uniform vec2 mouse;
uniform sampler2D texture;
uniform float amount;

float sineIn(float t) {
  return sin((t - 1.0) * HALF_PI) + 1.0;
}

float map(float value, float inMin, float inMax, float outMin, float outMax) {
  return outMin + (outMax - outMin) * (value - inMin) / (inMax - inMin);
}

float quarticIn(float t) {
  return pow(t, 4.0);
}    
    
float cubicIn(float t) {
  return t * t * t;
}

void main(void)
{
    // Normalized pixel coordinates (from 0 to 1)
    vec2 uv = gl_FragCoord.xy/resolution.xy;
	
	//rotate
	/*
	vec2 textureOffset = vec2(-0.5);
    uv = vec2((uv.x - 0.5) * (resolution.x / resolution.y), uv.y - 0.5);
	float amountf = amount/resolution.x;
	uv *= (2. - (resolution.x / resolution.y)) / 2.;
    uv *= mat2(cos(amountf), sin(amountf), -sin(amountf), cos(amountf));
    uv.y *= resolution.x/resolution.y;
    uv = uv - textureOffset;
	*/
	
    float cha = map(uv.y,0.,1.,0. - mouse.y/resolution.y, 1. + mouse.y/resolution.y);

    cha = mix(cha,cubicIn(cha),mouse.x/resolution.x);
    //cha = map(cha,0.,1.,0. + uv.x * mouse.x/resolution.x,1. - uv.x * mouse.x/resolution.x);
   
    if(cha < 0. || cha > 1.) cha = 0.;
    uv.y = cha;  
    vec4 cole = texture2D(texture, uv);
	if(uv.y == 0.) cole = vec4(0.);
    gl_FragColor = cole;

}
