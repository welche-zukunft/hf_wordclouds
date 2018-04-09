#ifndef HALF_PI
#define HALF_PI 1.5707963267948966
#endif

#define PI 3.14159265359
#define PROCESSING_TEXTURE_SHADER

varying vec4 vertTexCoord;

uniform vec2 resolutionIn;
uniform sampler2D texture;

uniform float rotateL;
uniform float rotateR;

uniform float nonlinearL;
uniform float nonlinearR;

uniform vec2 lu1;
uniform vec2 ru1;
uniform vec2 ro1;
uniform vec2 lo1;

uniform vec2 lu2;
uniform vec2 ru2;
uniform vec2 ro2;
uniform vec2 lo2;

float sineIn(float t) {
  return sin((t - 1.0) * HALF_PI) + 1.0;
}

float quarticIn(float t) {
  return pow(t, 4.0);
}

float cubicIn(float t) {
  return t * t * t;
}

float map(float value, float inMin, float inMax, float outMin, float outMax) {
  return outMin + (outMax - outMin) * (value - inMin) / (inMax - inMin);
}


mat2 rotate2d(float _angle){
    return mat2(cos(_angle),-sin(_angle),
                sin(_angle),cos(_angle));
}


float cross2( in vec2 a, in vec2 b ) {
	return a.x*b.y - a.y*b.x;
}

vec2 invBilinear( in vec2 p, in vec2 a, in vec2 b, in vec2 c, in vec2 d ){
    vec2 e = b-a;
    vec2 f = d-a;
    vec2 g = a-b+c-d;
    vec2 h = p-a;

    float k2 = cross2( g, f );
    float k1 = cross2( e, f ) + cross2( h, g );
    float k0 = cross2( h, e );

    float w = k1*k1 - 4.0*k0*k2;

    if( w<0.0 ) return vec2(-1.0);

    w = sqrt( w );

    float v1 = (-k1 - w)/(2.0*k2);
    float v2 = (-k1 + w)/(2.0*k2);
    float u1 = (h.x - f.x*v1)/(e.x + g.x*v1);
    float u2 = (h.x - f.x*v2)/(e.x + g.x*v2);
    bool  b1 = v1>0.0 && v1<1.0 && u1>0.0 && u1<1.0;
    bool  b2 = v2>0.0 && v2<1.0 && u2>0.0 && u2<1.0;

    vec2 res = vec2(-1.0);

    if(  b1 && !b2 ) res = vec2( u1, v1 );
    if( !b1 &&  b2 ) res = vec2( u2, v2 );
    
    return res;
}

void main(void){
	float middle = (resolutionIn.x / resolutionIn.y) / 2.; //middlepoint
	float delta = middle / 2.;
	float p1 = middle - delta;
	float p2 = middle + delta;
	vec2 offset = vec2(p1,0.5);
	vec2 offset2 = vec2(p2,0.5);

	// Normalized pixel coordinates (from 0 to 1)
    vec2 uv = gl_FragCoord.xy/resolutionIn.xy;
    uv = vertTexCoord.xy;
	uv.y = 1.-uv.y;
	vec2 uv2 = uv;

	uv.x *= (resolutionIn.x / resolutionIn.y);
	uv2.x *= (resolutionIn.x / resolutionIn.y);

	uv -= offset;
	uv2 -= offset2;
	
    uv = rotate2d( sin(rotateL*0.01)*PI ) * uv;
	uv2 = rotate2d( sin(rotateR*0.01)*PI ) * uv2;

    uv += offset;
	uv2 += offset2;
	
	uv.x /= (resolutionIn.x  / resolutionIn.y);
	uv2.x /= (resolutionIn.x  / resolutionIn.y);

	vec2 texUv = invBilinear(uv,lu1,ru1,ro1,lo1);
	texUv.y = mix(texUv.y,sineIn(texUv.y),nonlinearL);
	vec3 col = texture2D(texture, texUv).xyz;
	if(texUv.x < 0. || texUv.x > 0.5) col = vec3(0.,0.,0.);
	if(texUv.y < 0. || texUv.y > 1.) col = vec3(0.,0.,0.);

	vec2 texUv2 = invBilinear(uv2,lu2,ru2,ro2,lo2);
	texUv2.y = mix(texUv2.y,sineIn(texUv2.y),nonlinearR);
	vec3 col2 = texture2D(texture, texUv2).xyz;
	if(texUv2.x < 0.5 || texUv2.x > 1.) col2 = vec3(0.,0.,0.);
	if(texUv2.y < 0. || texUv2.y > 1.) col2 = vec3(0.,0.,0.);

    gl_FragColor = vec4(col + col2,1.);


}
