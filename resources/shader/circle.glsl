
varying vec4 vertTexCoord;

uniform vec2 resolution;
uniform float time;


void main(void)
{
    float aa = (244.0*-(cos(time)*0.2)+500.0)/resolution.y;
	vec2 uv = (gl_FragCoord.xy-(resolution.xy/2.0))/resolution.xx;
    float gr = dot(uv,uv);

    float cr = (resolution.y/2.0)/resolution.x;
    vec2 weight = vec2(cr*cr+cr*aa,cr*cr-cr*aa);

    gl_FragColor = vec4(vec3(clamp((gr-weight.y)/(weight.x-weight.y),0.4,1.0)),1.0);

}
